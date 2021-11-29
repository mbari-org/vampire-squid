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

package org.mbari.vars.vam.api

import java.net.URI
import java.time.{Duration, Instant}
import java.util.UUID

import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.dao.jpa.ByteArrayConverter
import org.scalatra.{BadRequest, NotFound}
import org.scalatra.swagger.Swagger

import scala.jdk.CollectionConverters._
import scala.concurrent.ExecutionContext

/**
  * @author Brian Schlining
  * @since 2017-03-06T17:08:00
  */
class MediaV1Api(controller: MediaController)(
    implicit val executor: ExecutionContext
) extends APIStack {

  post("/") {
    validateRequest()
    val videoSequenceName = params
      .get("video_sequence_name")
      .getOrElse(halt(BadRequest("""{error: "A 'video_sequence_name' parameter is required"}""")))
    val cameraId = params
      .get("camera_id")
      .getOrElse(halt(BadRequest("""{error: "A 'camera_id' parameter is required"}""")))
    val videoName = params
      .get("video_name")
      .getOrElse(halt(BadRequest("""{error: "A 'video_name' parameter is required"}""")))
    val uri = params
      .getAs[URI]("uri")
      .getOrElse(halt(BadRequest("""{error: "A 'uri' parameter is required"}""")))
    val start = params
      .getAs[Instant]("start_timestamp")
      .getOrElse(halt(BadRequest("""{error: "A 'start_timestamp' parameter is required"}""")))
    val duration                 = params.getAs[Duration]("duration_millis")
    val container                = params.get("container")
    val videoCodec               = params.get("video_codec")
    val audioCodec               = params.get("audio_codec")
    val width                    = params.getAs[Int]("width")
    val height                   = params.getAs[Int]("height")
    val frameRate                = params.getAs[Double]("frame_rate")
    val sizeBytes                = params.getAs[Long]("size_bytes")
    val description              = params.get("description") // VideoReference description
    val videoDescription         = params.get("video_description")
    val videoSequenceDescription = params.get("video_sequence_description")
    val sha512                   = params.getAs[Array[Byte]]("sha512")
    validateRequest()
    controller
      .create(
        videoSequenceName,
        cameraId,
        videoName,
        uri,
        start,
        duration,
        container,
        videoCodec,
        audioCodec,
        width,
        height,
        frameRate,
        sizeBytes,
        description,
        sha512,
        videoSequenceDescription,
        videoDescription
      )
      .map(controller.toJson)
  }

  put("/") {
    validateRequest()
    val sha512 = params
      .getAs[Array[Byte]]("sha512")
      .getOrElse(halt(BadRequest("""{error: "A 'sha512' parameter is required"}""")))
    val videoSequenceName = params
      .get("video_sequence_name")
      .getOrElse(halt(BadRequest("""{error: "A 'video_sequence_name' parameter is required"}""")))
    val cameraId = params
      .get("camera_id")
      .getOrElse(halt(BadRequest("""{error: "A 'camera_id' parameter is required"}""")))
    val videoName = params
      .get("video_name")
      .getOrElse(halt(BadRequest("""{error: "A 'video_name' parameter is required"}""")))
    val uri                      = params.getAs[URI]("uri")
    val start                    = params.getAs[Instant]("start_timestamp")
    val duration                 = params.getAs[Duration]("duration_millis")
    val container                = params.get("container")
    val videoCodec               = params.get("video_codec")
    val audioCodec               = params.get("audio_codec")
    val width                    = params.getAs[Int]("width")
    val height                   = params.getAs[Int]("height")
    val frameRate                = params.getAs[Double]("frame_rate")
    val sizeBytes                = params.getAs[Long]("size_bytes")
    val description              = params.get("description") // VideoReference description
    val videoDescription         = params.get("video_description")
    val videoSequenceDescription = params.get("video_sequence_description")
    validateRequest()
    controller
      .update(
        sha512,
        videoSequenceName,
        cameraId,
        videoName,
        uri,
        start,
        duration,
        container,
        videoCodec,
        audioCodec,
        width,
        height,
        frameRate,
        sizeBytes,
        description,
        videoSequenceDescription,
        videoDescription
      )
      .map(opt => opt.map(controller.toJson).getOrElse("{}"))

  }

  put("/:video_reference_uuid") {
    validateRequest()
    val videoReferenceUuid = params
      .getAs[UUID]("video_reference_uuid")
      .getOrElse(halt(BadRequest("""{"error":"A 'video_reference_uuid' is required"}""")))
    val sha512                   = params.getAs[Array[Byte]]("sha512")
    val videoSequenceName        = params.get("video_sequence_name")
    val cameraId                 = params.get("camera_id")
    val videoName                = params.get("video_name")
    val uri                      = params.getAs[URI]("uri")
    val start                    = params.getAs[Instant]("start_timestamp")
    val duration                 = params.getAs[Duration]("duration_millis")
    val container                = params.get("container")
    val videoCodec               = params.get("video_codec")
    val audioCodec               = params.get("audio_codec")
    val width                    = params.getAs[Int]("width")
    val height                   = params.getAs[Int]("height")
    val frameRate                = params.getAs[Double]("frame_rate")
    val sizeBytes                = params.getAs[Long]("size_bytes")
    val description              = params.get("description")
    val videoDescription         = params.get("video_description")
    val videoSequenceDescription = params.get("video_sequence_description")

    controller
      .findByVideoReferenceUuid(videoReferenceUuid)
      .map {
        case None =>
          halt(
            NotFound(
              s"""{"error": "A media with a video_reference_uuid of $videoReferenceUuid does not exist""""
            )
          )
        case Some(m) =>
          controller
            .findAndUpdate(
              d => d.findByUUID(videoReferenceUuid),
              videoSequenceName.getOrElse(m.videoSequenceName),
              cameraId.getOrElse(m.cameraId),
              videoName.getOrElse(m.videoName),
              sha512,
              uri,
              start,
              duration,
              container,
              videoCodec,
              audioCodec,
              width,
              height,
              frameRate,
              sizeBytes,
              description,
              videoSequenceDescription,
              videoDescription
            )
            .map(opt => opt.map(controller.toJson).getOrElse("{}"))
      }

  }

  get("/sha512/:sha512") {
    val shaString = params
      .get("sha512")
      .getOrElse(halt(BadRequest("""{error: "A hex encoded SHA512 checksum is required"}""")))
    val sha = ByteArrayConverter.decode(shaString)
    controller
      .findBySha512(sha)
      .map({
        case None =>
          halt(NotFound("""{not_found: "A video with matching checksum was not found"}"""))
        case Some(v) => controller.toJson(v)
      })
  }

  get("/videoreference/:uuid") {
    val uuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'uuid' parameter is required"}""")))
    controller
      .findByVideoReferenceUuid(uuid)
      .map({
        case None =>
          halt(
            NotFound(
              s"""not_found: "A media with a video-reference uuid of $uuid was not found"}"""
            )
          )
        case Some(v) => controller.toJson(v)
      })
  }

  get("/videoreference/filename/:filename") {
    val filename = params
      .get("filename")
      .getOrElse(halt(BadRequest("""{error: "A 'filename' parameter is required"}""")))
    controller
      .findByFileName(filename)
      .map(_.asJava)
      .map(controller.toJson)
  }

  get("/videosequence/:name") {
    val name = params
      .get("name")
      .getOrElse(halt(BadRequest("""{error: "A 'video_sequence_name' parameter is required"}""")))
    controller
      .findByVideoSequenceName(name)
      .map(_.asJava)
      .map(controller.toJson)
  }

  get("/video/:name") {
    val name = params
      .get("name")
      .getOrElse(halt(BadRequest("""{error: "A 'video_name' parameter is required"}""")))
    controller
      .findByVideoName(name)
      .map(_.asJava)
      .map(controller.toJson)
  }

  // TODO need a method to find by camera id and between datatimes
  get("/camera/:camera_id/:start_time/:end_time") {
    val cameraId = params
      .get("camera_id")
      .getOrElse(halt(BadRequest("""{error: "A 'camera_id' parameter is required"}""")))

    val startTime = params
      .getAs[Instant]("start_time")
      .getOrElse(
        halt(
          BadRequest(
            """{error: "A 'start_time' parameter in UTC time formated as 'yyyy-mm-ddThh:MM:ssZ' is required"}"""
          )
        )
      )

    val endTime = params
      .getAs[Instant]("end_time")
      .getOrElse(
        halt(
          BadRequest(
            """{error: "An 'end_time' parameter in UTC time formated as 'yyyy-mm-ddThh:MM:ssZ' is required"}"""
          )
        )
      )

    controller
      .findByCameraIdAndTimestamps(cameraId, startTime, endTime)
      .map(_.asJava)
      .map(controller.toJson)

  }

  get("/concurrent/:uuid") {
    val videoReferenceUuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'video_reference_uuid' parameter is required"}""")))
    controller
      .findConcurrent(videoReferenceUuid)
      .map(_.asJava)
      .map(controller.toJson)
  }

  get("/camera/:camera_id/:datetime") {
    val cameraId = params
      .get("camera_id")
      .getOrElse(halt(BadRequest("""{error: "A 'camera_id' parameter is required"}""")))
    val ts = params
      .getAs[Instant]("datetime")
      .getOrElse(
        halt(
          BadRequest(
            """{error: "An 'datetime' parameter in UTC time formated as 'yyyy-mm-ddThh:MM:ssZ' is required"}"""
          )
        )
      )
    controller
      .findByCameraIdAndTimestamp(cameraId, ts)
      .map(_.asJava)
      .map(controller.toJson)
  }

  get("/uri/*") {
    val uri = params
      .getAs[URI]("splat")
      .getOrElse(halt(BadRequest("""{error: "A 'uri' parameter is required"}""")))
    controller
      .findByURI(uri)
      .map({
        case None =>
          halt(NotFound(s"""{not_found: "A video with a url matching $uri was not found"}"""))
        case Some(v) => controller.toJson(v)
      })

  }

}
