/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vampiresquid.repository.jpa

import jakarta.persistence.{EntityManager, FlushModeType}
import org.slf4j.LoggerFactory
import org.mbari.vampiresquid.etc.jdk.Logging.given


import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
 * Implicits used in this package
 *
 * @author
 *   Brian Schlining
 * @since 2016-05-06T13:34:00
 */
object extensions:

    private val log = System.getLogger(getClass().getName)

    extension (entityManager: EntityManager)
        def runTransaction[R](fn: EntityManager => R)(implicit ec: ExecutionContext): Future[R] =
            Future:
                val transaction = entityManager.getTransaction
                transaction.begin()
                try
                    val n = fn.apply(entityManager)
                    transaction.commit()
                    n
                catch
                    case NonFatal(e) =>
                        log.atError.withCause(e).log("Error running transaction")
                        throw e
                finally if transaction.isActive then transaction.rollback()

        /**
         * Runs a read-only transaction that does not flush changes to the database. This prevents Hibernate from
         * attempting to UPDATE entities that were loaded but not explicitly modified, which is important when running
         * with a read-only database connection.
         */
        def runReadOnlyTransaction[R](fn: EntityManager => R)(implicit ec: ExecutionContext): Future[R] =
            Future:
                runReadOnlyTransactionSync(fn)

        /**
         * Synchronous version of runReadOnlyTransaction. Sets flush mode to COMMIT (no auto-flush) and rolls back the
         * transaction instead of committing to ensure no changes are persisted. Also sets read-only hints at both the
         * Hibernate session and JDBC connection levels.
         */
        def runReadOnlyTransactionSync[R](fn: EntityManager => R): R =
            val originalFlushMode = entityManager.getFlushMode
            val transaction       = entityManager.getTransaction

            // Get underlying Hibernate session and set read-only hints
            val session = entityManager.unwrap(classOf[org.hibernate.Session])
            session.doWork { connection =>
                connection.setReadOnly(true)
            }
            session.setDefaultReadOnly(true)

            transaction.begin()
            try
                entityManager.setFlushMode(FlushModeType.COMMIT)
                val n = fn.apply(entityManager)
                // Rollback instead of commit to ensure no changes are persisted
                transaction.rollback()
                n
            catch
                case NonFatal(e) =>
                    log.atError.withCause(e).log("Error running read-only transaction")
                    throw e
            finally
                entityManager.setFlushMode(originalFlushMode)
                session.setDefaultReadOnly(false)
                session.doWork { connection =>
                    connection.setReadOnly(false)
                }
                if transaction.isActive then transaction.rollback()

