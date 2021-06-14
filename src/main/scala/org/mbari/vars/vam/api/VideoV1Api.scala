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
    implicit val executor: ExecutionContext
) extends APIStack {


  get("/?") {
    controller
      .findAll
      .map(_.asJava)
      .map(controller.toJson)
  }


  get("/:uuid") {
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


  get("/videosequence/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a Video Sequence UUID")))
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

  get("/videoreference/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a Video Reference UUID")))
    controller.findByVideoReferenceUUID(uuid)
      .map({
        case None => 
          halt(NotFound(s"A video containing a videoreference with a UUID of $uuid was not found in the database"))
        case Some(v) =>
          controller.toJson(v)
      })
  }

  get("/lastupdate/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a Video UUID")))
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


  get("/name/:name") {
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


  get("/timestamp/:timestamp") {
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


  get("/timestamp/:start/:end") {
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


  delete("/:uuid") {
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

  post("/") {
    validateRequest()
    val name = params
      .get("name")
      .getOrElse(halt(BadRequest("""{error: "A 'name' parameter is required"}""")))
    val videoSequenceUUID = params
      .getAs[UUID]("video_sequence_uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'video_sequence_uuid' parameter is required"}""")))
    val startTimestamp = params
      .getAs[Instant]("start") // Allow alternatives names for old API
      .orElse(params.getAs[Instant]("start_timestamp"))
      .getOrElse(
        halt(BadRequest("""{error: "A 'start' or 'start_timestamp' parameter is required"}"""))
      )
    val duration    = params.getAs[Duration]("duration_millis")
    val description = params.get("description")
    controller
      .create(videoSequenceUUID, name, startTimestamp, duration, description)
      .map(controller.toJson)
  }


  put("/:uuid") {
    validateRequest()
    val uuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'uuid' parameter is required"}""")))
    val name        = params.get("name")
    val description = params.get("description")
    val start = params
      .getAs[Instant]("start")
      .orElse(params.getAs[Instant]("start_timestamp"))
    val duration          = params.getAs[Duration]("duration_millis")
    val videoSequenceUUID = params.getAs[UUID]("video_sequence_uuid")
    controller
      .update(uuid, name, start, duration, description, videoSequenceUUID)
      .map(controller.toJson)
  }

}
