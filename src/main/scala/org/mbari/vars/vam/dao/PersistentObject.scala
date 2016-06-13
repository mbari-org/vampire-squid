package org.mbari.vars.vam.dao

import java.util.UUID

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T16:21:00
 */
trait PersistentObject {

  def primaryKey: Option[UUID]
}
