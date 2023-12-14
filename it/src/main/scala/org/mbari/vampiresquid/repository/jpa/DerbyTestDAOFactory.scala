/*
 * Copyright 2021 MBARI
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

object DerbyTestDAOFactory extends SpecDAOFactory:

    override def testProps(): Map[String, String] =
        TestDAOFactory.TestProperties ++
            Map(
                "hibernate.dialect"                                     -> "org.hibernate.dialect.DerbyDialect",
                "hibernate.hbm2ddl.auto"                                -> "create",
                "jakarta.persistence.schema-generation.database.action" -> "create",
                "jakarta.persistence.schema-generation.scripts.action"  -> "drop-and-create"
            )

    lazy val entityManagerFactory: EntityManagerFactory =
        val driver   = config.getString("database.derby.driver")
        Class.forName(config.getString("database.derby.driver"))
        val url      = config.getString("database.derby.url")
        val user     = config.getString("database.derby.user")
        val password = config.getString("database.derby.password")
        EntityManagerFactories(url, user, password, driver, testProps())

object H2TestDAOFactory extends SpecDAOFactory:

    Class.forName(config.getString("database.h2.driver"))

    override def testProps(): Map[String, String] = TestDAOFactory.TestProperties ++
        Map(
            "jakarta.persistence.schema-generation.scripts.action" -> "drop-and-create"
        )

    lazy val entityManagerFactory: EntityManagerFactory =
        val driver   = config.getString("database.h2.driver")
        val url      = config.getString("database.h2.url")
        val user     = config.getString("database.h2.user")
        val password = config.getString("database.h2.password")
        EntityManagerFactories(url, user, password, driver, testProps())
