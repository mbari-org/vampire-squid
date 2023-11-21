package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.DerbyTestDAOFactory

class DerbyVideoSequenceControllerSuite extends VideoSequenceControllerITSuite:
  override val daoFactory = DerbyTestDAOFactory