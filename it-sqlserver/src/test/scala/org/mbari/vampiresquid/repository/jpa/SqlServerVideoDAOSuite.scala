package org.mbari.vampiresquid.repository.jpa

class SqlServerVideoDAOSuite extends VideoDAOITSuite:
  override def daoFactory = SqlServerDAOFactory
