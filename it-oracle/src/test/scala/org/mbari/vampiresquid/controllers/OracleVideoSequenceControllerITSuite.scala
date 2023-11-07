package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.OracleDAOFactory

class OracleVideoSequenceControllerITSuite extends VideoSequenceControllerITSuite:
  override def daoFactory = OracleDAOFactory