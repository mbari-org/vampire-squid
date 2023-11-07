package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.SqlServerDAOFactory

class SqlServerMediaControllerITSuite extends MediaControllerITSuite:
  override def daoFactory = SqlServerDAOFactory
