package org.mbari.vars.vam.dao

import java.util.UUID

/**
 * We have made a design decision to always use UUIDs (aka GUIDs) as the primary key. This requirement
 * is enforced so that external applications can rely on the use of that key. All persistent objects
 * should implement this trait
 *
 * @author Brian Schlining
 * @since 2016-05-05T16:21:00
 */
trait PersistentObject {

  def primaryKey: Option[UUID]
}
