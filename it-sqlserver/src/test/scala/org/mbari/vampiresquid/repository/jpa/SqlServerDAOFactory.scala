package org.mbari.vampiresquid.repository.jpa

import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.utility.DockerImageName
import jakarta.persistence.EntityManagerFactory
import org.testcontainers.containers.JdbcDatabaseContainer
import org.mbari.vampiresquid.etc.tc.AzureSqlEdgeContainerProvider

object SqlServerDAOFactory extends SpecDAOFactory:

  val container = new AzureSqlEdgeContainerProvider().newInstance()

  // THe image name must match the one in src/test/resources/container-license-acceptance.txt
  // val container = new MSSQLServerContainer(DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-latest"))
  // container.acceptLicense()
  container.withInitScript("sql/mssqlserver/02_m3_video_assets.sql")
  container.withReuse(true)

  override def beforeAll(): Unit = container.start()
  override def afterAll(): Unit  = container.stop()

  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect" -> "org.hibernate.dialect.SQLServerDialect"
      )

  lazy val entityManagerFactory: EntityManagerFactory =
    val driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    Class.forName(driver)
    EntityManagerFactories(
      container.getJdbcUrl(),
      container.getUsername(),
      container.getPassword(),
      container.getDriverClassName(),
      testProps()
    )