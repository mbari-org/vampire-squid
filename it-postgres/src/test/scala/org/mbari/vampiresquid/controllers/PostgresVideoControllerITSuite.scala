package org.mbari.vampiresquid.controllers


import org.mbari.vampiresquid.repository.jpa.PostgresqlTestDAOFactory

class PostgresVideoControllerITSuite extends VideoControllerITSuite:
  override def daoFactory = PostgresqlTestDAOFactory
  override def beforeAll(): Unit =
    super.beforeAll()
    println(s"---- Is Postgres up? ${PostgresqlTestDAOFactory.container.isRunning()}")
