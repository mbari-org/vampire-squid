/*
 * Copyright 2021 MBARI
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

import org.mbari.vampiresquid.repository.jpa.entity.{Media as MediaDTO, VideoReferenceEntity}

import java.net.URI
import java.time.{Duration, Instant}
import java.util.UUID
import scala.util.Try
import java.util.HexFormat
import org.mbari.vampiresquid.etc.sdk.FormTransform
import org.mbari.vampiresquid.etc.sdk.ToStringTransforms

case class Media(
    video_sequence_uuid: Option[UUID] = None,
    video_uuid: Option[UUID] = None,
    video_reference_uuid: Option[UUID] = None,
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
    description: Option[String] = None, // video_reference_description
    video_sequence_description: Option[String] = None,
    video_description: Option[String] = None,
    sha512: Option[Array[Byte]] = None
):

    def videoSequenceUuid: UUID                  = video_sequence_uuid.orNull
    def videoUuid: UUID                          = video_uuid.orNull
    def videoReferenceUuid: UUID                 = video_reference_uuid.orNull
    def videoSequenceName: String                = video_sequence_name.orNull
    def cameraId: String                         = camera_id.orNull
    def videoName: String                        = video_name.orNull
    def startTimestamp: Instant                  = start_timestamp.orNull
    def videoCodec: Option[String]               = video_codec
    def audioCodec: Option[String]               = audio_codec
    def frameRate: Option[Double]                = frame_rate
    def sizeBytes: Option[Long]                  = size_bytes
    def videoSequenceDescription: Option[String] = video_sequence_description
    def videoDescription: Option[String]         = video_description
    lazy val duration: Option[Duration]          = duration_millis.map(Duration.ofMillis)
    lazy val endTimestamp: Option[Instant]       = duration.flatMap(d => start_timestamp.map(_.plus(d)))
    def contains(ts: Instant): Boolean           =
        val opt = for
            e <- endTimestamp
            s <- start_timestamp
        yield s == ts || e == ts || (s.isBefore(ts) && e.isAfter(ts))
        opt.getOrElse(false)

    def equalValues(that: Media): Boolean =
        video_sequence_name == that.video_sequence_name &&
            camera_id == that.camera_id &&
            video_name == that.video_name &&
            uri == that.uri &&
            start_timestamp == that.start_timestamp &&
            duration_millis == that.duration_millis &&
            container == that.container &&
            video_codec == that.video_codec &&
            audio_codec == that.audio_codec &&
            width == that.width &&
            height == that.height &&
            frame_rate == that.frame_rate &&
            size_bytes == that.size_bytes &&
            description == that.description &&
            video_sequence_description == that.video_sequence_description &&
            video_description == that.video_description &&
            sha512 == that.sha512

object Media:

    private val hex = HexFormat.of()

    def from(mediaDto: MediaDTO): Media =
        Media(
            Option(mediaDto.videoSequenceUuid),
            Option(mediaDto.videoUuid),
            Option(mediaDto.videoReferenceUuid),
            Option(mediaDto.videoSequenceName),
            Option(mediaDto.cameraId),
            Option(mediaDto.videoName),
            Option(mediaDto.uri),
            Option(mediaDto.startTimestamp),
            Option(mediaDto.duration).map(_.toMillis),
            Option(mediaDto.container),
            Option(mediaDto.videoCodec),
            Option(mediaDto.audioCodec),
            Try(mediaDto.width.toInt).toOption,
            Try(mediaDto.height.toInt).toOption,
            Try(mediaDto.frameRate.toDouble).toOption,
            Try(mediaDto.sizeBytes.toLong).toOption,
            Option(mediaDto.description),
            Option(mediaDto.videoSequenceDescription),
            Option(mediaDto.videoDescription),
            Option(mediaDto.sha512)
        )

    def from(videoReferenceEntity: VideoReferenceEntity): Media =
        // To avoid instantiating all the JPA lazy relations, we use
        // only the VideoReference case class as it does most conversions
        // from Java to Scala Option types for us.
        val videoEntity         = videoReferenceEntity.getVideo
        val videoReference      = VideoReference.from(videoReferenceEntity)
        val videoSequenceEntity = videoEntity.getVideoSequence
        Media(
            Option(videoSequenceEntity.getUuid),
            Option(videoEntity.getUuid),
            Option(videoReferenceEntity.getUuid()),
            Option(videoSequenceEntity.getName),
            Option(videoSequenceEntity.getCameraID),
            Option(videoEntity.getName),
            Option(videoReference.uri),
            Option(videoEntity.getStart),
            Option(videoEntity.getDuration).map(_.toMillis),
            videoReference.container,
            videoReference.video_codec,
            videoReference.audio_codec,
            videoReference.width,
            videoReference.height,
            videoReference.frame_rate,
            videoReference.size_bytes,
            videoReference.description,
            Option(videoSequenceEntity.getDescription),
            Option(videoEntity.getDescription),
            videoReference.sha512
        )

    def from(videoSequence: VideoSequence, videoReference: VideoReference): Media =
        val video = videoSequence.videos.filter(vr => vr.video_references.contains(videoReference)).head
        val temp  = build(
            videoSequenceName = Some(videoSequence.name),
            cameraId = Some(videoSequence.camera_id),
            videoName = Some(video.name),
            uri = Some(videoReference.uri),
            startTimestamp = Some(video.start),
            duration = video.duration,
            container = videoReference.container,
            videoCodec = videoReference.video_codec,
            audioCodec = videoReference.audio_codec,
            width = videoReference.width,
            height = videoReference.height,
            frameRate = videoReference.frame_rate,
            sizeBytes = videoReference.size_bytes,
            description = videoReference.description,
            sha512 = videoReference.sha512
        )
        temp.copy(
            video_sequence_uuid = Option(videoSequence.uuid),
            video_uuid = Option(video.uuid),
            video_reference_uuid = Option(videoReference.uuid)
        )

    def build(
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
    ): Media =
        Media(
            None,
            None,
            None,
            videoSequenceName,
            cameraId,
            videoName,
            uri,
            startTimestamp,
            duration.map(_.toMillis),
            container,
            videoCodec,
            audioCodec,
            width,
            height,
            frameRate,
            sizeBytes,
            description,
            videoSequenceDescription,
            videoDescription,
            sha512
        )

    def fromFormMap(map: Map[String, String]): Media =
        val durationMillis = Try(map.get("duration_millis").map(_.toLong)).toOption.flatten
        Media(
            map.get("video_sequence_uuid").map(UUID.fromString),
            map.get("video_reference_uuid").map(UUID.fromString),
            map.get("video_uuid").map(UUID.fromString),
            map.get("video_sequence_name"),
            map.get("camera_id"),
            map.get("video_name"),
            map.get("uri").map(URI.create),
            map.get("start_timestamp").map(Instant.parse),
            durationMillis, // Try(map.get("duration_millis").map(_.toLong)).getOrElse(None).tap(println),
            map.get("container"),
            map.get("video_codec"),
            map.get("audio_codec"),
            map.get("width").map(_.toInt),
            map.get("height").map(_.toInt),
            map.get("frame_rate").map(_.toDouble),
            map.get("size_bytes").map(_.toLong),
            map.get("description"),
            map.get("video_sequence_description"),
            map.get("video_description"),
            map.get("sha512").map(hex.parseHex(_))
        )

    def toFormMap(m: Media): String =

        import ToStringTransforms.{transform, given}
        import FormTransform.given

        transform(m)
