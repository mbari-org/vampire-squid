package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerVideoSequenceControllerSuite extends VideoSequenceControllerITSuite:
  override def daoFactory = SqlServerDAOFactory