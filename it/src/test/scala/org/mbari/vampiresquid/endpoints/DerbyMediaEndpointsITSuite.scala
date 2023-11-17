package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyMediaEndpointsITSuite extends MediaEndpointsITSuite {
  override val daoFactory = DerbyTestDAOFactory
  
}
