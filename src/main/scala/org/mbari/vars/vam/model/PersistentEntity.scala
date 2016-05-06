package org.mbari.vars.vam.model

import java.util.UUID

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-05T11:01:00
  */
trait PersistentEntity {

  def uuid: UUID
}
