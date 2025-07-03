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

import jakarta.persistence.EntityManagerFactory
import org.mbari.vampiresquid.DatabaseParams
import org.mbari.vampiresquid.etc.flyway.FlywayMigrator
import org.testcontainers.containers.PostgreSQLContainer

object PostgresqlTestDAOFactory extends SpecDAOFactory:

    // TODO - intialize the container with SQL so UUID type gets correctly created
    val container = new PostgreSQLContainer("postgres:17")
//    container.withInitScript("sql/postgresql/02_m3_video_assets.sql")
    container.withReuse(true)
    container.start()

    //  override def beforeAll(): Unit = container.start()
    //  override def afterAll(): Unit  = container.stop()
    // NOTE: calling container.stop() after each test causes the tests to lose the connection to the database.
    // I'm using a shutdown hook to close the container at the end of the tests.
    //  override def afterAll(): Unit  = container.stop()
    Runtime.getRuntime.addShutdownHook(new Thread(() => container.stop()))

    override def testProps(): Map[String, String] =
        TestDAOFactory.TestProperties ++
            Map(
                "hibernate.dialect" -> "org.hibernate.dialect.PostgreSQLDialect",
                "jakarta.persistence.schema-generation.scripts.action" -> "drop-and-create"
            )

    lazy val entityManagerFactory: EntityManagerFactory =
        Class.forName(container.getDriverClassName)
        EntityManagerFactories(
            container.getJdbcUrl(),
            container.getUsername(),
            container.getPassword(),
            container.getDriverClassName(),
            testProps()
        )
