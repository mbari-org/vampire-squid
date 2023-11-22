package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerVideoSequenceEndpointsSuite extends VideoSequenceEndpointsITSuite {
    override val daoFactory = SqlServerDAOFactory

}
