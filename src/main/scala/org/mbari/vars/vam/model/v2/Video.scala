package org.mbari.vars.vam.model.v2

import java.time.Instant
import java.util.UUID

import org.mbari.vars.vam.model.PersistentEntity

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T11:09:00
 */
case class Video(
  uuid: UUID,
  name: String,
  start: Instant,
  durationSeconds: Double,
  videoSequence: VideoSequence,
  videoViews: Seq[VideoView]) extends PersistentEntity