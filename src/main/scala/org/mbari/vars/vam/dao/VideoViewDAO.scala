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
trait VideoViewDAO extends DAO[VideoView] {

  def findByVideoSequenceUUID(uuid: UUID): Iterable[VideoView]
  def findByVideoUUID(uuid: UUID): Iterable[VideoView]
  def findByTimestamp(timestamp: Instant): Iterable[VideoView]
  def findByVideoSequenceUUIDAndTimestamp(uuid: UUID, timestamp: Instant): Iterable[VideoView]
  def findByURI(uri: URI): Option[VideoView]

}
