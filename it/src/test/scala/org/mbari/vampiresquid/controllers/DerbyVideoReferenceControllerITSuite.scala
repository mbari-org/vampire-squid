package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyVideoReferenceControllerITSuite extends VideoReferenceControllerITSuite:
  override val daoFactory = DerbyTestDAOFactory

