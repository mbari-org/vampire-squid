package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.PostgresqlTestDAOFactory

class PostgresVideoReferenceControllerSuite extends VideoReferenceControllerITSuite {
  override def daoFactory = PostgresqlTestDAOFactory

}
