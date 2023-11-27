package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerTestDAOFactory

class SqlServerVideoReferenceControllerSuite extends VideoReferenceControllerITSuite {
  override def daoFactory = SqlServerTestDAOFactory

}
