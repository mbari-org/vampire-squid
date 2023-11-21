package org.mbari.vampiresquid.repository.jpa

class DerbyVideoSequenceDAOSuite extends VideoSequenceDAOITSuite {
  def daoFactory: SpecDAOFactory = DerbyTestDAOFactory
}
