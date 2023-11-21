package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyVideoReferenceControllerSuite extends VideoReferenceControllerITSuite:
  override val daoFactory = DerbyTestDAOFactory

