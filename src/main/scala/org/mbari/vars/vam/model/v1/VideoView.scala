package org.mbari.vars.vam.model.v1

import java.net.URI
import java.time.Instant
import java.util.UUID

import org.mbari.vars.vam.model.PersistentEntity

case class VideoView(
  uuid: UUID,
  uri: URI,
  container: String,
  videoCodec: String,
  audioCodec: String,
  width: Int,
  height: Int,
  video: UUID,
  lastUpdated: Instant = Instant.now()) extends PersistentEntity

object VideoView {

  def apply(uri: URI, video: UUID): VideoView = this(uri, null, null, null, 0, 0, video)

  def apply(
    uri: URI,
    container: String,
    videoCodec: String,
    audioCodec: String,
    width: Int,
    height: Int,
    video: UUID): VideoView = VideoView(UUID.randomUUID(), uri, container, videoCodec, audioCodec, width, height, video)

}
