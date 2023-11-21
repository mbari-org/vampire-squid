package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerVideoControllerSuite extends VideoControllerITSuite:
  override def daoFactory = SqlServerDAOFactory
