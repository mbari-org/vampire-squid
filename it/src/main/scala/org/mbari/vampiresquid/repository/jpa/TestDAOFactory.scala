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

import java.util.concurrent.TimeUnit
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * @author
 *   Brian Schlining
 * @since 2017-03-06T11:44:00
 */
object TestDAOFactory:

    val TestProperties = EntityManagerFactories.PRODUCTION_PROPS ++ Map(
        "jakarta.persistence.schema-generation.scripts.create-target" -> "target/test-database-create.ddl",
        "jakarta.persistence.schema-generation.scripts.drop-target"   -> "target/test-database-drop.ddl"
    )

//     val Instance: SpecDAOFactory = DerbyTestDAOFactory

trait SpecDAOFactory extends JPADAOFactory:

    lazy val config = ConfigFactory.load()

    def beforeAll(): Unit = ()
    def afterAll(): Unit  = ()

    def cleanup(): Unit =

        import scala.concurrent.ExecutionContext.Implicits.global
        val dao = newVideoSequenceDAO()

        val f = dao.runTransaction(d =>
            // TODO we need a count method for each DAO
            val all = dao.findAll(0, 1000000)
            all.foreach(dao.delete)
        )
        f.onComplete(t => dao.close())
        Await.result(f, Duration(4, TimeUnit.SECONDS))

    def testProps(): Map[String, String]
