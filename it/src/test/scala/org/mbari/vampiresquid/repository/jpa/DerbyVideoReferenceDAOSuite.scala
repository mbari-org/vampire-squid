package org.mbari.vampiresquid.repository.jpa

class DerbyVideoReferenceDAOSuite extends VideoReferenceDAOITSuite {


  override def daoFactory: SpecDAOFactory = DerbyTestDAOFactory

  
}
