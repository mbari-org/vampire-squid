package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.PostgresqlDAOFactory

/**
  * THis is working as it's correctly creating uuid as uuid types. 
  * Other Postgres tests are creating uuid as binary, so all 
  * subsequent tests fail
  */
class PostgresMediaControllerITSuite extends MediaControllerITSuite:
  override def daoFactory = PostgresqlDAOFactory
