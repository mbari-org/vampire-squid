package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.PostgresqlTestDAOFactory

class PostgresVideoSequenceEndpointsSuite extends VideoSequenceEndpointsITSuite {
    override val daoFactory = PostgresqlTestDAOFactory

}
