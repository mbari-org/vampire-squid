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

import org.mbari.vars.vam.controllers.VideoSequenceController
import org.mbari.vars.vam.dao.jpa.VideoSequence
import org.scalatra.swagger._
import org.scalatra._

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

/**
  * Note that we're not using Scalatra's JSON support. We're rolling out own with GSON
  *
  * @author Brian Schlining
  * @since 2016-05-20T14:45:00
  */
class VideoSequenceV1Api(controller: VideoSequenceController)(
    implicit val swagger: Swagger,
    val executor: ExecutionContext
) extends APIStack {

  override protected def applicationDescription: String = "Video Sequence API (v1)"

  val vsGET = (apiOperation[Iterable[VideoSequence]]("findAll")
    summary "List all video sequences")

  get("/?", operation(vsGET)) {
    controller.findAll.map(vs => controller.toJson(vs.asJava))
  }

  val uuidGET = (apiOperation[VideoSequence]("findByUUID")
    summary "Find a video sequence by uuid"
    parameters (pathParam[UUID]("uuid").description("The UUID of the video sequence")))

  get("/:uuid", operation(uuidGET)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a valid UUID")))
    controller
      .findByUUID(uuid)
      .map({
        case None =>
          halt(
            NotFound(
              s"{error: 'A video-sequence with a UUID of $uuid was not found in the database'}"
            )
          )
        case Some(vs) => controller.toJson(vs)
      })
  }

  get("/lastupdate/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller
      .findByUUID(uuid)
      .map({
        case None =>
          val error = Map(
            "not_found" -> s"A video sequence with a UUID of $uuid was not found in the database"
          ).asJava
          halt(NotFound(controller.toJson(error)))
        case Some(v) =>
          v.lastUpdated match {
            case None =>
              val error = Map(
                "missing_value"       -> "No last updated timestamp was found",
                "video_sequence_uuid" -> uuid
              ).asJava
              halt(NotFound(controller.toJson(error)))
            case Some(t) =>
              val data = Map("timestamp" -> t.toString).asJava
              controller.toJson(data)
          }
      })
  }

  val nameGET = (apiOperation[VideoSequence]("findByName")
    summary "Find a video sequence by name"
    parameters (pathParam[String]("name").description("The name of the video sequence")))

  get("/name/:name", operation(nameGET)) {
    val name = params("name")
    controller
      .findByName(name)
      .map({
        case None => {
          halt(
            NotFound(
              s"{not_found: 'A video-sequence with a name of '$name' was not found in the database'}"
            )
          )
        }
        case Some(vs) => controller.toJson(vs)
      })
  }

  val namesGET = (apiOperation[String]("listNames")
    summary "List all names used by the video-sequences")

  get("/names", operation(namesGET)) {
    controller
      .findAllNames
      .map(_.asJava) // Transform to Java map for GSON
      .map(controller.toJson)
  }

  get("/names/camera/:camera_id") {
    val cameraID = params
      .get("camera_id")
      .getOrElse(halt(BadRequest("""{error: "A 'camera_id' parameter is required"}""")))
    controller
      .findAllNamesByCameraID(cameraID)
      .map(_.asJava) // Transform to Java map for GSON
      .map(controller.toJson)
  }

  val camerasGET = (apiOperation[String]("listCameras")
    summary "List all camera-ids used by the video-sequences")

  get("/cameras", operation(camerasGET)) {
    controller
      .findAllCameraIDs
      .map(_.asJava) // Transform to Java map for GSON
      .map(controller.toJson)
  }

  get("/camera/:camera_id") {
    val cameraID = params
      .get("camera_id")
      .getOrElse(halt(BadRequest("""{error: "A 'camera_id' parameter is required"}""")))
    controller
      .findByCameraId(cameraID)
      .map(controller.toJson)
  }

  val findGET = (apiOperation[Seq[VideoSequence]]("findByCameraIDAndTimestamp")
    summary "Find VideoSequences by camera-id and timestamp"
    parameters (pathParam[String]("camera_id").description("The camera-id of interest").required,
    pathParam[Instant]("timestamp").description("The timestamp of interest").required,
    Parameter(
      "window_millis",
      DataType.Long,
      Some("The search window in milliseconds"),
      required = false,
      defaultValue = Some("60")
    )))

  get("/camera/:camera_id/:timestamp", operation(findGET)) {
    val cameraID = params
      .get("camera_id")
      .getOrElse(halt(BadRequest("""{error: "A 'camera_id' parameter is required"}""")))
    val timestamp = params
      .getAs[Instant]("timestamp")
      .getOrElse(halt(BadRequest("""{error: "A 'timestamp' parameter is required"}""")))
    val window = params.getAs[Duration]("window_millis").getOrElse(Duration.ofMinutes(60L))
    controller
      .findByCameraIDAndTimestamp(cameraID, timestamp, window)
      .map(controller.toJson)
  }

  val vsPOST = (apiOperation[String]("create")
    summary "Create a video-sequence"
    parameters (Parameter(
      "name",
      DataType.String,
      Some("The unique name of the video-sequence"),
      paramType = ParamType.Body,
      required = true
    ),
    Parameter(
      "camera_id",
      DataType.String,
      Some("The name of the camera (e.g. Tiburon)"),
      paramType = ParamType.Body,
      required = true
    )))

  post("/", operation(vsPOST)) {
    validateRequest()
    val name = params
      .get("name")
      .getOrElse(halt(BadRequest("""{error: "A 'name' parameter is required"}""")))
    val cameraID = params
      .get("camera_id")
      .getOrElse(halt(BadRequest("""{error: "A 'camera_id' parameter is required"}""")))
    val description = params.get("description")
    controller
      .create(name, cameraID, description)
      .map(controller.toJson)
  }

  val vsDELETE = (apiOperation[Unit]("delete")
    summary "Delete a video-sequence. Also deletes associated videos and video-references"
    parameters (pathParam[UUID]("uuid")
      .description("The UUID of the video-sequence to be deleted")))

  delete("/:uuid", operation(vsDELETE)) {
    validateRequest()
    val uuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'uuid' parameter is required"}""")))
    controller
      .delete(uuid)
      .map({
        case true  => halt(NoContent())
        case false => halt(NotFound(s"Failed. No video-sequence with UUID of $uuid was found."))
      })
  }

  val vsPUT = (apiOperation[VideoSequence]("update")
    summary "Update a video-sequence"
    parameters (Parameter(
      "uuid",
      DataType.String,
      Some("The UUID of the video-sequence"),
      required = true,
      paramType = ParamType.Body
    ),
    Parameter(
      "name",
      DataType.String,
      Some("The new name of the video-sequence"),
      required = false,
      paramType = ParamType.Body
    ),
    Parameter(
      "camera_id",
      DataType.String,
      Some("The new cameraID of the video-sequence"),
      required = false,
      paramType = ParamType.Body
    ),
    Parameter(
      "description",
      DataType.String,
      Some("The new description of the video-sequence"),
      required = false,
      paramType = ParamType.Body
    )))

  put("/:uuid", operation(vsPUT)) {
    validateRequest()
    val uuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'uuid' parameter is required"}""")))
    val cameraID    = params.get("camera_id")
    val name        = params.get("name")
    val description = params.get("description")
    controller
      .update(uuid, name, cameraID, description)
      .map(controller.toJson)
  }

}
