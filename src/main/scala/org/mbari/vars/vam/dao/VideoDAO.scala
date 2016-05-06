package org.mbari.vars.vam.dao

import java.time.Instant
import java.util.UUID

import org.mbari.vars.vam.model.v1.Video

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-05T12:59:00
  */
trait VideoDAO extends DAO[Video] {

  def findByTimestamp(start: Instant): Iterable[Video]
  def findByName(name: String): Option[Video]
  def findByVideoSequenceUUID(uuid: UUID): Iterable[Video]

}
