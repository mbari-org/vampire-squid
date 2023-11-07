package org.mbari.vampiresquid.repository.jpa

class SqlServerVideoDAOSuite extends VideoDAOSuite:
  override def daoFactory = SqlServerDAOFactory
