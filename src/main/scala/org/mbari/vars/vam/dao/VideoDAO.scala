package org.mbari.vars.vam.dao

import java.time.{ Duration, Instant }
import java.util.UUID

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T12:59:00
 */
trait VideoDAO[T <: PersistentObject] extends DAO[T] {

  def findByTimestamp(start: Instant, window: Duration): Iterable[T]
  def findByName(name: String): Option[T]
  def findByVideoSequenceUUID(uuid: UUID): Iterable[T]
  def findByVideoReferenceUUID(uuid: UUID): Option[T]
  def findAllNames(): Iterable[String]
  def findAllNamesAndTimestamps(): Iterable[(String, Instant)]
  def findBetweenTimestamps(t0: Instant, t1: Instant): Iterable[T]

}
