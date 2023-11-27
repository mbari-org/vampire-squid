package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.SqlServerTestDAOFactory

class SqlServerVideoReferenceEndpointsSuite extends VideoReferenceEndpointsITSuite{
   
    override val daoFactory = SqlServerTestDAOFactory
}
