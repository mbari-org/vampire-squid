package org.mbari.vars.vam.model.v1

import java.time.Instant
import java.util.UUID

import org.mbari.vars.vam.model.PersistentEntity

case class Video(
  uuid: UUID,
  name: String,
  start: Instant,
  durationSeconds: Double,
  videoSequence: UUID,
  lastUpdated: Instant = Instant.now()) extends PersistentEntity

object Video {

  def apply(name: String, start: Instant, durationSeconds: Double): Video =
    this(name, start, durationSeconds, null)

  def apply(name: String, start: Instant, durationSeconds: Double, videoSequence: UUID): Video =
    Video(UUID.randomUUID(), name, start, durationSeconds, videoSequence)
}
