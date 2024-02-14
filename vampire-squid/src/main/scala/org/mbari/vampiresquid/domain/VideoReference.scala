/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
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

package org.mbari.vampiresquid.domain

import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity

import java.net.URI
import java.time.Instant
import java.util.UUID

case class VideoReferenceCreate(
    video_uuid: UUID,
    uri: URI,
    container: Option[String] = None,
    video_codec: Option[String] = None,
    audio_codec: Option[String] = None,
    width: Option[Int] = None,
    height: Option[Int] = None,
    frame_rate: Option[Double] = None,
    size_bytes: Option[Long] = None,
    sha512: Option[Array[Byte]] = None,
    description: Option[String] = None
)

case class VideoReferenceUpdate(
    video_uuid: Option[UUID] = None,
    uri: Option[URI] = None,
    container: Option[String] = None,
    video_codec: Option[String] = None,
    audio_codec: Option[String] = None,
    width: Option[Int] = None,
    height: Option[Int] = None,
    frame_rate: Option[Double] = None,
    size_bytes: Option[Long] = None,
    sha512: Option[Array[Byte]] = None,
    description: Option[String] = None
)

object VideoReferenceUpdate:
    def from(v: VideoReference, videoUuid: Option[UUID] = None): VideoReferenceUpdate =
        VideoReferenceUpdate(
            videoUuid,
            Option(v.uri),
            v.container,
            v.videoCodec,
            v.audioCodec,
            v.width,
            v.height,
            v.frameRate,
            v.sizeBytes,
            v.sha512,
            v.description
        )

case class VideoReference(
    uuid: UUID,
    uri: URI,
    container: Option[String] = None,
    video_codec: Option[String] = None,
    audio_codec: Option[String] = None,
    width: Option[Int] = None,
    height: Option[Int] = None,
    frame_rate: Option[Double] = None,
    size_bytes: Option[Long] = None,
    sha512: Option[Array[Byte]] = None,
    description: Option[String] = None,
    last_updated_time: Option[Instant] = None
):
    def videoCodec: Option[String]            = video_codec
    def audioCodec: Option[String]            = audio_codec
    def frameRate: Option[Double]             = frame_rate
    def sizeBytes: Option[Long]               = size_bytes
    def lastUpdatedTimestamp: Option[Instant] = last_updated_time

object VideoReference:
    def from(v: VideoReferenceEntity): VideoReference =
        VideoReference(
            v.getUuid,
            v.getUri,
            Option(v.getContainer),
            Option(v.getVideoCodec),
            Option(v.getAudioCodec),
            Option(v.getWidth).map(_.intValue()),
            Option(v.getHeight).map(_.intValue()),
            Option(v.getFrameRate).map(_.doubleValue()),
            Option(v.getSize).map(_.longValue()),
            Option(v.getSha512),
            Option(v.getDescription),
            Option(v.getLastUpdatedTime).map(_.toInstant)
        )
