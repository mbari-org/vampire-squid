package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.OracleDAOFactory

class OracleMediaControllerITSuite extends MediaControllerITSuite:
  override def daoFactory = OracleDAOFactory
