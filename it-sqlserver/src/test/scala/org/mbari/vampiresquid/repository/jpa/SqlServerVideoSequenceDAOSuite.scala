package org.mbari.vampiresquid.repository.jpa

class SqlServerVideoSequenceDAOSuite extends VideoSequenceDAOITSuite {
  override def daoFactory = SqlServerTestDAOFactory

}
