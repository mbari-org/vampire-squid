package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerMediaEndpointsSuite extends MediaEndpointsITSuite {
  override def daoFactory = SqlServerDAOFactory

}
