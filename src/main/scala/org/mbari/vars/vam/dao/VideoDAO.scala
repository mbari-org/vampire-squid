package org.mbari.vars.vam.dao

import java.time.{ Duration, Instant }
import java.util.UUID

import org.mbari.vars.vam.model.v1.Video

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T12:59:00
 */
trait VideoDAO[T <: PersistentObject[UUID]] extends DAO[UUID, T] {

  def findByTimestamp(start: Instant, window: Duration): Iterable[T]
  def findByName(name: String): Option[T]
  def findByVideoSequenceUUID(uuid: UUID): Iterable[T]
  def findByVideoReferenceUUID(uuid: UUID): Option[T]

}
