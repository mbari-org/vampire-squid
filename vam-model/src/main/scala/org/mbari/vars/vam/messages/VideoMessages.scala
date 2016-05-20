package org.mbari.vars.vam.messages

import java.time.{ Duration, Instant }
import java.util.UUID

trait OptionalVideoProperties {
  def name: String
  def start: Instant
  def duration: Duration // duration is encoded in JSON as Millis
  def videoSequence: UUID

  def nameOpt: Option[String] = Option(name)
  def startOpt: Option[Instant] = Option(start)
  def durationOpt: Option[Duration] = Option(duration)
  def videoSequenceOpt: Option[UUID] = Option(videoSequence) // used to move video
}

case class CreateVideo(
    videoSequence: UUID,
    name: String,
    start: Instant,
    duration: Duration) extends OptionalVideoProperties with Msg {
  require(name != null, "Name property can not be null")
  require(videoSequence != null, "VideoSequence (UUID) can not be null")
}

case class DeleteVideoByName(name: String) extends Msg

case class DeleteVideoByUUID(uuid: UUID) extends Msg

case class FindVideoByName(name: String) extends Msg

case class FindVideoByUUID(uuid: UUID) extends Msg

case class FindVideoByCameraIDAndTimestamp(cameraID: String, timestamp: Instant) extends Msg

case class UpdateVideoByUUID(
  uuid: UUID,
  name: String,
  start: Instant,
  duration: Duration,
  videoSequence: UUID = null) extends OptionalVideoProperties with Msg

case class UpdateVideoByName(
  name: String,
  start: Instant,
  duration: Duration,
  videoSequence: UUID = null) extends OptionalVideoProperties with Msg

