package org.mbari.vars.vam.messages

import java.net.URI
import java.util.UUID

trait OptionalVideoReferenceProperties {

  def container: String
  def videoCodec: String
  def audioCodec: String
  def width: Int
  def height: Int
  def video: UUID // Reference to the owning Video's primary key (a UUID)

  def containerOpt: Option[String] = Option(container)
  def videoCodecOpt: Option[String] = Option(videoCodec)
  def audioCodecOpt: Option[String] = Option(audioCodec)
  def widthOpt: Option[Int] = Option(width)
  def heightOpt: Option[Int] = Option(height)
  def videoOpt: Option[UUID] = Option(video)

}

case class CreateVideoReference(video: UUID, uri: URI, container: String, videoCodec: String, audioCodec: String,
  width: Int = 0, height: Int = 0, frameRate: Double = 0, size: Long = 0)
    extends OptionalVideoReferenceProperties with Msg {
  require(uri != null, "URI Property can not be null")
  require(video != null, "Video (UUID) property can not be null")
}

case class DeleteVideoReferenceByURI(uri: URI) extends Msg

case class DeleteVideoReferenceBYUUID(uuid: UUID) extends Msg

case class FindVideoReferenceByURI(uri: URI) extends Msg

case class FindVideoReferenceByUUID(uuid: UUID) extends Msg

case class UpdateVideoReferenceByUUID(uuid: UUID, uri: URI, container: String, videoCodec: String,
  audioCodec: String, width: Int, height: Int, video: UUID = null)
    extends OptionalVideoReferenceProperties with Msg

case class UpdateVideoReferenceByURI(uri: URI, container: String, videoCodec: String,
  audioCodec: String, width: Int, height: Int, video: UUID = null)
    extends OptionalVideoReferenceProperties with Msg

