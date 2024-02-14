package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.OracleDAOFactory

class OracleVideoControllerITSuite extends VideoControllerITSuite:
  override def daoFactory = OracleDAOFactory