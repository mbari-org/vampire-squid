package org.mbari.vars.vam.dao

import java.time.{ Duration, Instant }
import java.util.UUID

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T12:54:00
 */
trait VideoSequenceDAO[T <: PersistentObject] extends DAO[T] {

  def findByName(name: String): Option[T]
  def findByCameraID(cameraID: String): Iterable[T]
  def findByVideoUUID(uuid: UUID): Option[T]
  def findByTimestamp(timestamp: Instant, window: Duration): Iterable[T]
  def findByNameAndTimestamp(name: String, timestamp: Instant, window: Duration): Iterable[T]
  def findByCameraIDAndTimestamp(cameraID: String, timestamp: Instant, window: Duration): Iterable[T]
  def findAllNames(): Iterable[String]
  def findAllCameraIDs(): Iterable[String]

}
