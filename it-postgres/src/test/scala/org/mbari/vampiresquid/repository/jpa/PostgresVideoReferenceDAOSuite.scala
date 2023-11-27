package org.mbari.vampiresquid.repository.jpa

class PostgresVideoReferenceDAOSuite extends VideoReferenceDAOITSuite {
  override def daoFactory = PostgresqlTestDAOFactory

}
