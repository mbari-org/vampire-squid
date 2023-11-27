package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerTestDAOFactory

class SqlServerVideoControllerSuite extends VideoControllerITSuite:
  override def daoFactory = SqlServerTestDAOFactory
