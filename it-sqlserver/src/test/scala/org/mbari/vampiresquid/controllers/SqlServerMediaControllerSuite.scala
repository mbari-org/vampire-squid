package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerMediaControllerSuite extends MediaControllerITSuite:
  override def daoFactory = SqlServerDAOFactory
