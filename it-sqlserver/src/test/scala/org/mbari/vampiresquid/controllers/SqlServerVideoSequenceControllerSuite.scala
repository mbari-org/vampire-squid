package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerTestDAOFactory

class SqlServerVideoSequenceControllerSuite extends VideoSequenceControllerITSuite:
  override def daoFactory = SqlServerTestDAOFactory
