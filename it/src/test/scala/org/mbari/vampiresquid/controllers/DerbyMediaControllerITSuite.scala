package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyMediaControllerSuite extends MediaControllerITSuite:
  override val daoFactory = DerbyTestDAOFactory