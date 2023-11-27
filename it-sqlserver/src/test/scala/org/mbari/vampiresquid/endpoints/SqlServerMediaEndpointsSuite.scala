package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.SqlServerTestDAOFactory

class SqlServerMediaEndpointsSuite extends MediaEndpointsITSuite {
  override def daoFactory = SqlServerTestDAOFactory

}
