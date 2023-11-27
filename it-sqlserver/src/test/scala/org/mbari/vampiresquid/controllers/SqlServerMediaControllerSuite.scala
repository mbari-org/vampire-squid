package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerTestDAOFactory

class SqlServerMediaControllerSuite extends MediaControllerITSuite:
  override def daoFactory = SqlServerTestDAOFactory
