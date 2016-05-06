package org.mbari.vars.vam.dao

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T16:21:00
 */
trait PersistentObject[A] {

  def primaryKey: Option[A]
}
