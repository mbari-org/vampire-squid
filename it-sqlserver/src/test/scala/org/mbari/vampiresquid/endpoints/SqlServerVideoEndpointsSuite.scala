package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.SqlServerTestDAOFactory

class SqlServerVideoEndpointsSuite extends VideoEndpointsITSuite {
  override def daoFactory = SqlServerTestDAOFactory
}
