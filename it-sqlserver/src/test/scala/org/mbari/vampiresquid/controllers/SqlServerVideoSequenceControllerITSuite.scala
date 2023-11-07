package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerVideoSequenceControllerITSuite extends VideoSequenceControllerITSuite:
  override def daoFactory = SqlServerDAOFactory