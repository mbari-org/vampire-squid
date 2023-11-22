package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerVideoEndpointsSuite extends VideoEndpointsITSuite {
  override def daoFactory = SqlServerDAOFactory
}
