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

package org.mbari.vars.vam.domain


import java.net.URI
import java.util.UUID


import java.time.Instant

import org.mbari.vars.vam.model.MutableMedia
import java.time.Duration

final case class Media(
  video_sequence_uuid: Option[UUID] = None,
  video_reference_uuid: Option[UUID] = None,
  video_uuid: Option[UUID] = None,
  video_sequence_name: Option[String] = None,
  camera_id: Option[String] = None,
  video_name: Option[String] = None,
  uri: Option[URI] = None,
  start_timestamp: Option[Instant] = None,
  duration_millis: Option[Long] = None,
  container: Option[String] = None,
  video_codec: Option[String] = None,
  audio_codec: Option[String] = None,
  width: Option[Int] = None,
  height: Option[Int] = None,
  frame_rate: Option[Double] = None,
  size_bytes: Option[Long] = None,
  description: Option[String] = None,
  sha512: Option[Array[Byte]] = None,
  video_sequence_description: Option[String] = None,
  video_description: Option[String] = None
) {

  lazy val duration: Option[Duration] = duration_millis.map(Duration.ofMillis)
  lazy val end_timestamp: Option[Instant] = if (start_timestamp.isDefined && duration.isDefined) {
    start_timestamp.map(_.plus(duration.get))
  } else None

  def contains(ts: Instant): Boolean = {
    end_timestamp match {
      case None => start_timestamp.get == ts
      case Some(e) =>
        start_timestamp.get == ts || e == ts || start_timestamp.get.isBefore(ts) && e.isAfter(ts)
    }
  }
}

object Media:

  def from(m: MutableMedia): Media = {
    Media(Option(m.videoReferenceUuid), 
      Option(m.videoReferenceUuid), 
      Option(m.videoUuid), 
      Option(m.videoSequenceName), 
      Option(m.cameraId), 
      Option(m.videoName), 
      Option(m.uri), 
      Option(m.startTimestamp), 
      Option(m.duration).map(_.toMillis), 
      Option(m.container), 
      Option(m.videoCodec), 
      Option(m.audioCodec), 
      Option(m.width), 
      Option(m.height), 
      Option(m.frameRate), 
      Option(m.sizeBytes), 
      Option(m.description), 
      Option(m.sha512), 
      Option(m.videoSequenceDescription), 
      Option(m.videoDescription)
    )
  }

