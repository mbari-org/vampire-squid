package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.PostgresqlDAOFactory

class PopstgresVideoSequenceEndpointsSuite extends VideoSequenceEndpointsITSuite {
    override val daoFactory = PostgresqlDAOFactory

}
