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
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.OracleContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.containers.MSSQLServerContainer

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

  val Instance: SpecDAOFactory = DerbyTestDAOFactory


trait SpecDAOFactory extends JPADAOFactory:

  lazy val config = ConfigFactory.load()

  def beforeAll(): Unit = ()
  def afterAll(): Unit = ()

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

object PostgresqlDAOFactory extends SpecDAOFactory:

  // TODO - intialize the container with SQL so UUID type gets correctly created
  val container         = new PostgreSQLContainer("postgres:16")
  container.withInitScript("sql/postgresql/02_m3_video_assets.sql")

  override def beforeAll(): Unit = container.start()
  override def afterAll(): Unit  = container.stop()

  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect"             -> "org.hibernate.dialect.PostgreSQLDialect",
      )

  lazy val entityManagerFactory: EntityManagerFactory =
    val driver   = "org.postgresql.Driver"
    Class.forName(container.getDriverClassName)
    EntityManagerFactories(container.getJdbcUrl(), 
      container.getUsername(), 
      container.getPassword(), 
      container.getDriverClassName(), 
      testProps())

object OracleDAOFactory extends SpecDAOFactory:

  val container =  new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"));

  override def beforeAll(): Unit = container.start()
  override def afterAll(): Unit  = container.stop()

  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect"             -> "org.hibernate.dialect.Oracle12cDialect",
      )

  lazy val entityManagerFactory: EntityManagerFactory =
    // val driver   = "org.postgresql.Driver"
    // Class.forName(container.getDriverClassName)
    EntityManagerFactories(container.getJdbcUrl(), 
      container.getUsername(), 
      container.getPassword(), 
      container.getDriverClassName(), 
      testProps())


object SqlServerDAOFactory extends SpecDAOFactory:

  // THe image name must match the one in src/test/resources/container-license-acceptance.txt
  val container =  new MSSQLServerContainer(DockerImageName.parse("mcr.microsoft.com/mssql/server:2017-CU12"))
  container.withInitScript("sql/mssqlserver/02_m3_video_assets.sql")
        

  override def beforeAll(): Unit = container.start()
  override def afterAll(): Unit  = container.stop()

  
  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect"             -> "org.hibernate.dialect.SQLServerDialect",
      )

  lazy val entityManagerFactory: EntityManagerFactory =
    val driver   = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    Class.forName(driver)
    EntityManagerFactories(container.getJdbcUrl(), 
      container.getUsername(), 
      container.getPassword(), 
      container.getDriverClassName(), 
      testProps())

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


