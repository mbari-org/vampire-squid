package org.mbari.vampiresquid.controllers


import org.mbari.vampiresquid.repository.jpa.PostgresqlDAOFactory

class PostgresVideoControllerITSuite extends VideoControllerITSuite:
  override def daoFactory = PostgresqlDAOFactory
  override def beforeAll(): Unit =
    super.beforeAll()
    println(s"---- Is Postgres up? ${PostgresqlDAOFactory.container.isRunning()}")
