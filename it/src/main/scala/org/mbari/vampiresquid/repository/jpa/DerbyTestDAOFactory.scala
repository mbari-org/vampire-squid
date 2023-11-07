package org.mbari.vampiresquid.repository.jpa

import jakarta.persistence.EntityManagerFactory

object DerbyTestDAOFactory extends SpecDAOFactory:

  override def testProps(): Map[String, String] =
    TestDAOFactory.TestProperties ++
      Map(
        "hibernate.dialect"                                     -> "org.hibernate.dialect.DerbyDialect",
        "hibernate.hbm2ddl.auto"                                -> "create",
        "jakarta.persistence.schema-generation.database.action" -> "create"
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

