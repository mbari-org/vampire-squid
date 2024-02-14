package org.mbari.vampiresquid.repository.jpa

import jakarta.persistence.EntityManagerFactory
import org.testcontainers.containers.OracleContainer
import org.testcontainers.utility.DockerImageName

object OracleDAOFactory extends SpecDAOFactory:

  val container = new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"))
  container.withInitScript("sql/oracle/02_m3_video_assets.sql")
  container.withReuse(true)

  override def beforeAll(): Unit = container.start()
  override def afterAll(): Unit  = container.stop()

  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect" -> "org.hibernate.dialect.Oracle12cDialect"
      )

  lazy val entityManagerFactory: EntityManagerFactory =
    // val driver   = "org.postgresql.Driver"
    // Class.forName(container.getDriverClassName)
    EntityManagerFactories(
      container.getJdbcUrl(),
      container.getUsername(),
      container.getPassword(),
      container.getDriverClassName(),
      testProps()
    )
