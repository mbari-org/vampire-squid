package org.mbari.vampiresquid.repository.jpa

class PostgresVideoSequenceDAOSuite extends VideoSequenceDAOITSuite {

  override def daoFactory = PostgresqlDAOFactory

}
