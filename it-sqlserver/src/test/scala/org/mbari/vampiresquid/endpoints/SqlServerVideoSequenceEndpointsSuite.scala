package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.SqlServerTestDAOFactory

class SqlServerVideoSequenceEndpointsSuite extends VideoSequenceEndpointsITSuite {
    override val daoFactory = SqlServerTestDAOFactory

}
