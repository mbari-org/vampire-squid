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

import java.net.URI
import java.time.{Duration, Instant}
import java.util.UUID
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity

/**
 * @author
 *   Brian Schlining
 * @since 2017-03-06T09:28:00
 */
class MutableMedia:

    var videoSequenceUuid: UUID = scala.compiletime.uninitialized

    var videoReferenceUuid: UUID = scala.compiletime.uninitialized

    var videoUuid: UUID = scala.compiletime.uninitialized

    var videoSequenceName: String = scala.compiletime.uninitialized

    var cameraId: String = scala.compiletime.uninitialized

    var videoName: String = scala.compiletime.uninitialized

    var uri: URI = scala.compiletime.uninitialized

    var startTimestamp: Instant = scala.compiletime.uninitialized

    var duration: Duration = scala.compiletime.uninitialized

    def duration_millis: Long | Null = Option(duration).map(_.toMillis).orNull

    var container: String = scala.compiletime.uninitialized

    var videoCodec: String = scala.compiletime.uninitialized

    var audioCodec: String = scala.compiletime.uninitialized

    var width: Int = scala.compiletime.uninitialized

    var height: Int = scala.compiletime.uninitialized

    var frameRate: Double = scala.compiletime.uninitialized

    var sizeBytes: Long = scala.compiletime.uninitialized

    var description: String = scala.compiletime.uninitialized // VideoReference description

    var videoSequenceDescription: String = scala.compiletime.uninitialized

    var videoDescription: String = scala.compiletime.uninitialized

    var sha512: Array[Byte] = scala.compiletime.uninitialized

    def endTimestamp: Option[Instant] =
        if startTimestamp != null && duration != null then Some(startTimestamp.plus(duration))
        else None

    def contains(ts: Instant): Boolean =
        endTimestamp match
            case None    => startTimestamp == ts
            case Some(e) =>
                startTimestamp == ts || e == ts || startTimestamp.isBefore(ts) && e.isAfter(ts)

object MutableMedia:

    def apply(videoReference: VideoReferenceEntity): MutableMedia =
        val video         = videoReference.getVideo
        val videoSequence = video.getVideoSequence

        val m = new MutableMedia
        m.videoSequenceUuid = videoSequence.getUuid
        m.videoSequenceName = videoSequence.getName
        m.cameraId = videoSequence.getCameraID
        m.videoSequenceDescription = videoSequence.getDescription

        m.videoUuid = video.getUuid
        m.videoName = video.getName
        m.startTimestamp = video.getStart
        m.duration = video.getDuration
        m.videoDescription = video.getDescription

        m.videoReferenceUuid = videoReference.getUuid
        m.uri = videoReference.getUri
        m.container = videoReference.getContainer
        m.videoCodec = videoReference.getVideoCodec
        m.audioCodec = videoReference.getAudioCodec
        m.width = videoReference.getWidth
        m.height = videoReference.getHeight
        m.frameRate = videoReference.getFrameRate
        m.sizeBytes = videoReference.getSize
        m.description = videoReference.getDescription
        m.sha512 = videoReference.getSha512

        m

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
    ): MutableMedia =
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
