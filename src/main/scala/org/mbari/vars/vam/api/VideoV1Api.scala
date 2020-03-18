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

import java.time.{Duration, Instant}
import java.util.UUID

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.controllers.VideoController
import org.mbari.vars.vam.dao.jpa.{Video, VideoSequence}
import org.scalatra.{BadRequest, NoContent, NotFound}
import org.scalatra.swagger.{DataType, ParamType, Parameter, Swagger}

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-24T13:41:00
  */
class VideoV1Api(controller: VideoController)(
    implicit
    val swagger: Swagger,
    val executor: ExecutionContext
) extends APIStack {
  override protected def applicationDescription: String = "Video API (v1)"

  val vGET = (apiOperation[Iterable[Video]]("findAll")
    summary "List all videos")

  get("/?", operation(vGET)) {
    controller
      .findAll
      .map(_.asJava)
      .map(controller.toJson)
  }

  val uuidGET = (apiOperation[Video]("findByUUID")
    summary "Find a video by uuid"
    parameters (pathParam[UUID]("uuid").description("The UUID of the video")))

  get("/:uuid", operation(uuidGET)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller
      .findByUUID(uuid)
      .map({
        case None =>
          halt(
            NotFound(
              s"""{not_found: "A video with a UUID of $uuid was not found in the database"}"""
            )
          )
        case Some(v) => controller.toJson(v)
      })
  }

  val videoSequenceUUIDGet = (apiOperation[VideoSequence]("findVideoSequenceByVideoUUID")
    summary "Find a videosequence by video's uuid"
    parameters (pathParam[UUID]("uuid").description("The UUID of the video")))

  get("/videosequence/:uuid", operation(videoSequenceUUIDGet)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller
      .findByUUID(uuid)
      .map({
        case None =>
          halt(
            NotFound(
              s"""{not_found: "A video with a UUID of $uuid was not found in the database"}"""
            )
          )
        case Some(v) => controller.toJson(v.videoSequence)
      })
  }

  get("/lastupdate/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller
      .findByUUID(uuid)
      .map({
        case None =>
          val error =
            Map("not_found" -> s"A video with a UUID of $uuid was not found in the database").asJava
          halt(NotFound(controller.toJson(error)))
        case Some(v) =>
          v.lastUpdated match {
            case None =>
              val error = Map(
                "missing_value" -> "No last updated timestamp was found",
                "video_uuid"    -> uuid
              ).asJava
              halt(NotFound(controller.toJson(error)))
            case Some(t) =>
              val data = Map("timestamp" -> t.toString).asJava
              controller.toJson(data)
          }
      })
  }

  val nameGET = (apiOperation[Video]("findByName")
    summary "Find a video by name"
    parameters (pathParam[String]("name").description("The name of the video")))

  get("/name/:name", operation(nameGET)) {
    val name = params.get("name").getOrElse(halt(BadRequest("Please provide a name")))
    controller
      .findByName(name)
      .map({
        case None =>
          val error = Map(
            "not_found" -> s"A video with a name of '$name' was not found in the database"
          ).asJava
          halt(NotFound(controller.toJson(error)))
        case Some(v) => controller.toJson(v)
      })
  }

  get("/names/videosequence/:name") {
    val name = params.get("name").getOrElse(halt(BadRequest("Please provide a name")))
    controller
      .findNamesByVideoSequenceName(name)
      .map(_.asJava)
      .map(controller.toJson)
  }

  val timestampGET = (apiOperation[Iterable[Video]]("findByTimestamp")
    summary "Find videos by timestamp"
    parameters (pathParam[String]("timestamp")
      .description("A UTC timestamp (yyyy-mm-ddThh:mm:ssZ)"),
    Parameter(
      "window_minutes",
      DataType.Long,
      Some("The search windows in minutes"),
      required = false,
      defaultValue = Some(Constants.DEFAULT_DURATION_WINDOW.toMinutes.toString)
    )))

  get("/timestamp/:timestamp", operation(timestampGET)) {
    val timestamp = params
      .getAs[Instant]("timestamp")
      .getOrElse(halt(BadRequest("""{error: "A 'timestamp' parameter is required"}""")))
    val window = Try(Duration.ofMinutes(params.getAs[Long]("window_minutes").get))
      .getOrElse(Constants.DEFAULT_DURATION_WINDOW)
    controller
      .findByTimestamp(timestamp, window)
      .map(_.asJava)
      .map(controller.toJson)
  }

  val timerangeGET = (apiOperation[Iterable[Video]]("findBetweenTimestamps")
    summary "Find videos between timestamps"
    parameters (pathParam[String]("start").description("A UTC timestamp (yyyy-mm-ddThh:mm:ssZ)"),
    pathParam[String]("end").description("A UTC timestamp (yyyy-mm-ddThh:mm:ssZ)")))

  get("/timestamp/:start/:end", operation(timerangeGET)) {
    val startTime = params
      .getAs[Instant]("start")
      .getOrElse(halt(BadRequest("""{error: "A 'start' parameter is required"}""")))
    val endTime = params
      .getAs[Instant]("end")
      .getOrElse(halt(BadRequest("""{error: "An 'end' parameter is required"}""")))
    controller
      .findBetweenTimestamps(startTime, endTime)
      .map(_.asJava)
      .map(controller.toJson)
  }

  // TODO delete should require authentication
  val vDELETE = (apiOperation[Unit]("delete")
    summary "Delete a video. Also deletes associated video-references"
    parameters (pathParam[UUID]("uuid").description("The UUID of the video to be deleted")))

  delete("/:uuid", operation(vDELETE)) {
    validateRequest()
    val uuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'uuid' parameter is required"}""")))
    controller
      .delete(uuid)
      .map({
        case true => halt(NoContent()) // Success! Deleted video with uuid
        case false =>
          halt(
            NotFound(
              s"""{not_found: "A video with a UUID of $uuid was not found in the database"}"""
            )
          )
      })
  }

  // TODO create should require authentication
  val vPOST = (apiOperation[String]("create")
    summary "Create a video"
    parameters (Parameter(
      "name",
      DataType.String,
      Some("The unique name of the video"),
      paramType = ParamType.Body,
      required = true
    ),
    Parameter(
      "video_sequence_uuid",
      DataType.String,
      Some("The uuid of the owning video-sequence"),
      paramType = ParamType.Body,
      required = true
    ),
    Parameter(
      "start",
      DataType.String,
      Some("The start time of the video as 'yyyy-mm-ddThh:mm:ssZ'"),
      paramType = ParamType.Body,
      required = true
    ),
    Parameter(
      "duration_millis",
      DataType.Long,
      Some("The duration of the video in milliseconds"),
      paramType = ParamType.Body,
      required = false
    ),
    Parameter(
      "description",
      DataType.String,
      Some("A description of the video"),
      paramType = ParamType.Body,
      required = false
    )))

  post("/", operation(vPOST)) {
    validateRequest()
    val name = params
      .get("name")
      .getOrElse(halt(BadRequest("""{error: "A 'name' parameter is required"}""")))
    val videoSequenceUUID = params
      .getAs[UUID]("video_sequence_uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'video_sequence_uuid' parameter is required"}""")))
    val start = params
      .getAs[Instant]("start")
      .getOrElse(halt(BadRequest("""{error: "A 'start' parameter is required"}""")))
    val duration    = params.getAs[Duration]("duration_millis")
    val description = params.get("description")
    controller
      .create(videoSequenceUUID, name, start, duration, description)
      .map(controller.toJson)
  }

  // TODO update should require authentication
  val vPUT = (apiOperation[Video]("update")
    summary "Update a video"
    parameters (pathParam[UUID]("The UUID of the video to be updated"),
    Parameter(
      "name",
      DataType.String,
      Some("The unique name of the video"),
      paramType = ParamType.Body,
      required = false
    ),
    Parameter(
      "video_sequence_uuid",
      DataType.String,
      Some("The uuid of the owning video-sequence"),
      paramType = ParamType.Body,
      required = false
    ),
    Parameter(
      "start",
      DataType.String,
      Some("The start time of the video as 'yyyy-mm-ddThh:mm:ssZ'"),
      paramType = ParamType.Body,
      required = false
    ),
    Parameter(
      "duration_millis",
      DataType.Long,
      Some("The duration of the video in milliseconds"),
      paramType = ParamType.Body,
      required = false
    ),
    Parameter(
      "description",
      DataType.String,
      Some("A description of the video"),
      paramType = ParamType.Body,
      required = false
    )))

  put("/:uuid", operation(vPUT)) {
    validateRequest()
    val uuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'uuid' parameter is required"}""")))
    val name              = params.get("name")
    val description       = params.get("description")
    val start             = params.getAs[Instant]("start")
    val duration          = params.getAs[Duration]("duration_millis")
    val videoSequenceUUID = params.getAs[UUID]("video_sequence_uuid")
    controller
      .update(uuid, name, start, duration, description, videoSequenceUUID)
      .map(controller.toJson)
  }

}
