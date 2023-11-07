package org.mbari.vampiresquid.repository.jpa


import jakarta.persistence.EntityManagerFactory
import org.testcontainers.containers.PostgreSQLContainer

object PostgresqlDAOFactory extends SpecDAOFactory:

  // TODO - intialize the container with SQL so UUID type gets correctly created
  val container = new PostgreSQLContainer("postgres:16")
  container.withInitScript("sql/postgresql/02_m3_video_assets.sql")
  container.withReuse(true)

  override def beforeAll(): Unit = container.start()
  override def afterAll(): Unit  = container.stop()

  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect" -> "org.hibernate.dialect.PostgreSQLDialect"
      )

  lazy val entityManagerFactory: EntityManagerFactory =
    val driver = "org.postgresql.Driver"
    Class.forName(container.getDriverClassName)
    EntityManagerFactories(
      container.getJdbcUrl(),
      container.getUsername(),
      container.getPassword(),
      container.getDriverClassName(),
      testProps()
    )