package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyVideoReferenceEndpointsSuite extends VideoReferenceEndpointsITSuite {

  override val daoFactory = DerbyTestDAOFactory
}
