package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyVideoSequenceEndpointsSuite extends VideoSequenceEndpointsITSuite {
    override val daoFactory = DerbyTestDAOFactory

}
