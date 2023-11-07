package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.PostgresqlDAOFactory

class PostgresVideoSequenceControllerITSuite extends VideoSequenceControllerITSuite:
  override def daoFactory = PostgresqlDAOFactory