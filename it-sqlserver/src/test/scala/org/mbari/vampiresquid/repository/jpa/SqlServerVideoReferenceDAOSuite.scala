package org.mbari.vampiresquid.repository.jpa

class SqlServerVideoReferenceDAOSuite extends VideoReferenceDAOITSuite {
  override def daoFactory = SqlServerDAOFactory

}
