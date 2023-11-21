package org.mbari.vampiresquid.repository.jpa

import jakarta.persistence.EntityManagerFactory
import org.mbari.vampiresquid.etc.tc.AzureSqlEdgeContainerProvider

object SqlServerDAOFactory extends SpecDAOFactory:

  val container = new AzureSqlEdgeContainerProvider().newInstance()

  // The image name must match the one in src/test/resources/container-license-acceptance.txt
  // val container = new MSSQLServerContainer(DockerImageName.parse("mcr.microsoft.com/mssql/server:2019-latest"))
  // container.acceptLicense()
  container.withInitScript("sql/mssqlserver/02_m3_video_assets.sql")
  container.withReuse(true)
  container.start()

//  override def beforeAll(): Unit = container.start()
  // NOTE: calling container.stop() after each test causes the tests to lose the connection to the database.
  // I'm using a shutdown hook to close the container at the end of the tests.
  //  override def afterAll(): Unit  = container.stop()
  Runtime.getRuntime.addShutdownHook(new Thread(() => container.stop()))

  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect" -> "org.hibernate.dialect.SQLServerDialect",
        "hibernate.hikari.idleTimeout" -> "1000",
        "hibernate.hikari.maxLifetime" -> "3000",
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