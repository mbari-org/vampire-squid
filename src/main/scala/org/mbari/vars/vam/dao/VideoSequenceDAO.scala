package org.mbari.vars.vam.dao

import java.time.Instant
import java.util.UUID

import org.mbari.vars.vam.model.v1.VideoSequence

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-05T12:54:00
  */
trait VideoSequenceDAO extends DAO[VideoSequence] {

  def findByName(name: String): Option[VideoSequence]
  def findByCameraID(cameraID: String): Iterable[VideoSequence]
  def findByVideoUUID(uuid: UUID): Option[VideoSequence]
  def findByTimestamp(timestamp: Instant): Iterable[VideoSequence]

}
