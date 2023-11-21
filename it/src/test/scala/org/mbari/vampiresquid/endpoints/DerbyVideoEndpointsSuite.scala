package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyVideoEndpointsSuite extends VideoEndpointsITSuite {
  override val daoFactory = DerbyTestDAOFactory
  
}
