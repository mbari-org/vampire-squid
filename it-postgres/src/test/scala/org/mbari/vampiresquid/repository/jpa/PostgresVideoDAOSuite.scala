package org.mbari.vampiresquid.repository.jpa

class PostgresVideoDAOSuite extends VideoDAOITSuite:
  override def daoFactory = PostgresqlTestDAOFactory
