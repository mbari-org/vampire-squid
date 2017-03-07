package org.mbari.vars.vam.model

import java.net.URI
import java.time.{ Duration, Instant }
import java.util.UUID

import com.google.gson.annotations.{ Expose, SerializedName }
import org.mbari.vars.vam.dao.jpa.VideoReference

/**
 * @author Brian Schlining
 * @since 2017-03-06T09:28:00
 */
class Media {

  @Expose(serialize = true)
  var videoSequenceUuid: UUID = _

  @Expose(serialize = true)
  var videoReferenceUuid: UUID = _

  @Expose(serialize = true)
  var videoUuid: UUID = _

  @Expose(serialize = true)
  var videoSequenceName: String = _

  @Expose(serialize = true)
  var cameraId: String = _

  @Expose(serialize = true)
  var videoName: String = _

  @Expose(serialize = true)
  var uri: URI = _

  @Expose(serialize = true)
  var start: Instant = _

  @Expose(serialize = true)
  @SerializedName(value = "duration_millis")
  var duration: Duration = _

  @Expose(serialize = true)
  var container: String = _

  @Expose(serialize = true)
  var videoCodec: String = _

  @Expose(serialize = true)
  var audioCodec: String = _

  @Expose(serialize = true)
  var width: Int = _

  @Expose(serialize = true)
  var height: Int = _

  @Expose(serialize = true)
  var frameRate: Double = _

  @Expose(serialize = true)
  var sizeBytes: Long = _

  @Expose(serialize = true)
  var description: String = _ // VideoReference description

  @Expose(serialize = true)
  var sha512: Array[Byte] = _

  def end: Option[Instant] = if (start != null && duration != null) Some(start.plus(duration))
  else None

  def contains(ts: Instant): Boolean = {
    end match {
      case None => start == ts
      case Some(e) => start == ts || e == ts || start.isBefore(ts) && e.isAfter(ts)
    }
  }

}

object Media {

  def apply(videoReference: VideoReference): Media = {
    val video = videoReference.video
    val videoSequence = video.videoSequence

    val m = new Media
    m.videoSequenceUuid = videoSequence.uuid
    m.videoSequenceName = videoSequence.name
    m.cameraId = videoSequence.cameraID

    m.videoUuid = video.uuid
    m.start = video.start
    m.duration = video.duration

    m.videoReferenceUuid = videoReference.uuid
    m.uri = videoReference.uri
    m.container = videoReference.container
    m.videoCodec = videoReference.videoCodec
    m.audioCodec = videoReference.audioCodec
    m.width = videoReference.width
    m.height = videoReference.height
    m.frameRate = videoReference.frameRate
    m.sizeBytes = videoReference.size
    m.description = videoReference.description
    m.sha512 = videoReference.sha512

    m
  }
}

