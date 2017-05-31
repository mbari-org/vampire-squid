package org.mbari.vars.vam.dao

import java.time.{ Duration, Instant }
import java.util.UUID

/**
 * Defines the API methods used for accessing Video objects
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

  /**
   * Finds videos with a start date between the 2 timestamps
   * @param t0
   * @param t1
   * @return
   */
  def findBetweenTimestamps(t0: Instant, t1: Instant): Iterable[T]

  /**
   * Get the names of the videos belonging to a particular video sequence
   * @param videoSequenceName
   * @return
   */
  def findNamesByVideoSequenceName(videoSequenceName: String): Iterable[String]

}
