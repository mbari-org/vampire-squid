package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.PostgresqlTestDAOFactory

class PostgresVideoSequenceControllerITSuite extends VideoSequenceControllerITSuite:
  override def daoFactory = PostgresqlTestDAOFactory
