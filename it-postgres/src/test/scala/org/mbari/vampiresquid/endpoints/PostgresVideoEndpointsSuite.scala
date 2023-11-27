package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.PostgresqlTestDAOFactory

class PostgresVideoEndpointsSuite extends VideoEndpointsITSuite {
  override def daoFactory = PostgresqlTestDAOFactory

}
