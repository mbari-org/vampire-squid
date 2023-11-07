package org.mbari.vampiresquid.repository.jpa

class PostgresVideoDAOSuite extends VideoDAOSuite:
  override def daoFactory = PostgresqlDAOFactory