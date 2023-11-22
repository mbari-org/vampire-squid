package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.PostgresqlDAOFactory

class PostgresMediaEndpointsSuite extends MediaEndpointsITSuite {
  override def daoFactory = PostgresqlDAOFactory

}
