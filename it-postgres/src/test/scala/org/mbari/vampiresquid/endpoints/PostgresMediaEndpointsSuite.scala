package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.PostgresqlTestDAOFactory

class PostgresMediaEndpointsSuite extends MediaEndpointsITSuite {
  override def daoFactory = PostgresqlTestDAOFactory

}
