package org.mbari.vampiresquid.repository.jpa


import jakarta.persistence.EntityManagerFactory
import org.testcontainers.containers.PostgreSQLContainer

object PostgresqlTestDAOFactory extends SpecDAOFactory:

  // TODO - intialize the container with SQL so UUID type gets correctly created
  val container = new PostgreSQLContainer("postgres:16")
  container.withInitScript("sql/postgresql/02_m3_video_assets.sql")
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
