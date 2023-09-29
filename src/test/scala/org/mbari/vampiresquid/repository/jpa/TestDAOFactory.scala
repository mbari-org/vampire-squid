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

import java.util.concurrent.TimeUnit
import jakarta.persistence.EntityManagerFactory
import com.typesafe.config.ConfigFactory
import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory.config

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * @author Brian Schlining
  * @since 2017-03-06T11:44:00
  */
object TestDAOFactory:

  val TestProperties = Map(
    "eclipselink.logging.level"                                 -> "FINE",
    "jakarta.persistence.schema-generation.scripts.action"        -> "drop-and-create",
    "jakarta.persistence.schema-generation.scripts.create-target" -> "target/test-database-create.ddl",
    "jakarta.persistence.schema-generation.scripts.drop-target"   -> "target/test-database-drop.ddl"
  )

  Class.forName("org.apache.derby.jdbc.ClientDriver")
  val Instance: SpecDAOFactory = DerbyTestDAOFactory


trait SpecDAOFactory extends JPADAOFactory:

  lazy val config = ConfigFactory.load()

  def cleanup(): Unit =

    import scala.concurrent.ExecutionContext.Implicits.global
    val dao = newVideoSequenceDAO()

    val f = dao.runTransaction(d => {
      val all = dao.findAll()
      all.foreach(dao.delete)
    })
    f.onComplete(t => dao.close())
    Await.result(f, Duration(4, TimeUnit.SECONDS))

  def testProps(): Map[String, String]

object DerbyTestDAOFactory extends SpecDAOFactory:

  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect"             -> "org.hibernate.dialect.DerbyDialect",
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

  override def testProps(): Map[String, String] = TestDAOFactory.TestProperties

  lazy val entityManagerFactory: EntityManagerFactory =
    val driver   = config.getString("database.h2.driver")
    val url      = config.getString("database.h2.url")
    val user     = config.getString("database.h2.user")
    val password = config.getString("database.h2.password")
    EntityManagerFactories(url, user, password, driver, testProps())


