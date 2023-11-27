package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.PostgresqlTestDAOFactory

class PostgresVideoReferenceEndpoints extends VideoReferenceEndpointsITSuite {
    override val daoFactory = PostgresqlTestDAOFactory
  
}
