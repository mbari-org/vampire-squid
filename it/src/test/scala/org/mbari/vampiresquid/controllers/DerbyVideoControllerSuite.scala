package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyVideoControllerSuite extends VideoControllerITSuite:
  override val daoFactory = DerbyTestDAOFactory
