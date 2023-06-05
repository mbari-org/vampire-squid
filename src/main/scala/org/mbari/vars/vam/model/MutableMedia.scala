/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vars.vam.model

import java.net.URI
import java.time.{Duration, Instant}
import java.util.UUID

import com.google.gson.annotations.{Expose, SerializedName}
import org.mbari.vars.vam.dao.jpa.VideoReferenceEntity

/**
  * @author Brian Schlining
  * @since 2017-03-06T09:28:00
  */
class MutableMedia {

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
  var startTimestamp: Instant = _

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
  var videoSequenceDescription: String = _

  @Expose(serialize = true)
  var videoDescription: String = _

  @Expose(serialize = true)
  var sha512: Array[Byte] = _

  def endTimestamp: Option[Instant] =
    if (startTimestamp != null && duration != null) Some(startTimestamp.plus(duration))
    else None

  def contains(ts: Instant): Boolean = {
    endTimestamp match {
      case None => startTimestamp == ts
      case Some(e) =>
        startTimestamp == ts || e == ts || startTimestamp.isBefore(ts) && e.isAfter(ts)
    }
  }

}

object MutableMedia {

  def apply(videoReference: VideoReferenceEntity): MutableMedia = {
    val video         = videoReference.video
    val videoSequence = video.videoSequence

    val m = new MutableMedia
    m.videoSequenceUuid = videoSequence.uuid
    m.videoSequenceName = videoSequence.name
    m.cameraId = videoSequence.cameraID
    m.videoSequenceDescription = videoSequence.description

    m.videoUuid = video.uuid
    m.videoName = video.name
    m.startTimestamp = video.start
    m.duration = video.duration
    m.videoDescription = video.description

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

  def build(
      videoReferenceUuid: Option[UUID] = None,
      videoSequenceUuid: Option[UUID] = None,
      videoUuid: Option[UUID] = None,
      videoSequenceName: Option[String] = None,
      cameraId: Option[String] = None,
      videoName: Option[String] = None,
      uri: Option[URI] = None,
      startTimestamp: Option[Instant] = None,
      duration: Option[Duration] = None,
      container: Option[String] = None,
      videoCodec: Option[String] = None,
      audioCodec: Option[String] = None,
      width: Option[Int] = None,
      height: Option[Int] = None,
      frameRate: Option[Double] = None,
      sizeBytes: Option[Long] = None,
      description: Option[String] = None,
      sha512: Option[Array[Byte]] = None,
      videoSequenceDescription: Option[String] = None,
      videoDescription: Option[String] = None
  ): MutableMedia = {
    val m = new MutableMedia
    videoReferenceUuid.foreach(m.videoReferenceUuid = _)
    videoSequenceUuid.foreach(m.videoSequenceUuid = _)
    videoUuid.foreach(m.videoUuid = _)
    videoSequenceName.foreach(m.videoSequenceName = _)
    cameraId.foreach(m.cameraId = _)
    videoName.foreach(m.videoName = _)
    uri.foreach(m.uri = _)
    startTimestamp.foreach(m.startTimestamp = _)
    duration.foreach(m.duration = _)
    container.foreach(m.container = _)
    videoCodec.foreach(m.videoCodec = _)
    audioCodec.foreach(m.audioCodec = _)
    width.foreach(m.width = _)
    height.foreach(m.height = _)
    frameRate.foreach(m.frameRate = _)
    sizeBytes.foreach(m.sizeBytes = _)
    description.foreach(m.description = _)
    sha512.foreach(m.sha512 = _)
    videoSequenceDescription.foreach(m.videoSequenceDescription = _)
    videoDescription.foreach(m.videoDescription = _)
    m
  }
}
