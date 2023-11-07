package org.mbari.vampiresquid.repository.jpa

class OracleVideoDAOSuite extends VideoDAOSuite:
  override def daoFactory = OracleDAOFactory