package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.PostgresqlDAOFactory

class PostgresVideoEndpointsSuite extends VideoEndpointsITSuite {
  override def daoFactory = PostgresqlDAOFactory

}
