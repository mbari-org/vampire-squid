package org.mbari.vars.vam.dao

import java.net.URI
import java.time.Instant
import java.util.UUID

import org.mbari.vars.vam.model.v1.VideoView

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T14:51:00
 */
trait VideoViewDAO[T <: PersistentObject[UUID]] extends DAO[UUID, T] {

  def findByVideoSequenceUUID(uuid: UUID): Iterable[T]
  def findByVideoUUID(uuid: UUID): Iterable[T]
  def findByTimestamp(timestamp: Instant): Iterable[T]
  def findByVideoSequenceUUIDAndTimestamp(uuid: UUID, timestamp: Instant): Iterable[T]
  def findByURI(uri: URI): Option[T]

}
