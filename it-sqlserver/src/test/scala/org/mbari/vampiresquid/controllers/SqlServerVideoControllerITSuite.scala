package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerVideoControllerITSuite extends VideoControllerITSuite:
  override def daoFactory = SqlServerDAOFactory
