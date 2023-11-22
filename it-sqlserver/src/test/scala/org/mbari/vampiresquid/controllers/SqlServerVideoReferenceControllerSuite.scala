package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerVideoReferenceControllerSuite extends VideoReferenceControllerITSuite {
  override def daoFactory = SqlServerDAOFactory

}
