package org.mbari.vars.vam.model.v1

import java.time.Instant
import java.util.UUID

import org.mbari.vars.vam.dao.PersistentObject
import org.mbari.vars.vam.model.PersistentEntity

case class VideoSequence(
    uuid: UUID,
    name: String,
    cameraID: String,
    lastUpdated: Instant = Instant.now()) extends PersistentObject[UUID] {
  override def primaryKey: Option[UUID] = Option(uuid)
}

object VideoSequence {
  def apply(name: String, cameraID: String): VideoSequence =
    VideoSequence(UUID.randomUUID(), name, cameraID)
}
