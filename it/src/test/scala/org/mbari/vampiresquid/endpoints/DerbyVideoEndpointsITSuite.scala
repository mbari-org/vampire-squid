package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyVideoEndpointsITSuite extends VideoEndpointsITSuite {
  override val daoFactory = DerbyTestDAOFactory
  
}
