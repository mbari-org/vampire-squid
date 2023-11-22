package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.PostgresqlDAOFactory

class PostgresVideoReferenceControllerSuite extends VideoReferenceControllerITSuite {
  override def daoFactory = PostgresqlDAOFactory

}
