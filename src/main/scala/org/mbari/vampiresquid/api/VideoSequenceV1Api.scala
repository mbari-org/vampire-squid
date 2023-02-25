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

package org.mbari.vampiresquid.api

import org.mbari.vampiresquid.controllers.VideoSequenceController
import org.scalatra._

import java.time.{Duration, Instant}
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

/**
  * Note that we're not using Scalatra's JSON support. We're rolling out own with GSON
  *
  * @author Brian Schlining
  * @since 2016-05-20T14:45:00
  */
class VideoSequenceV1Api(controller: VideoSequenceController)(
    implicit val executor: ExecutionContext
) extends APIStack {

  get("/?") {
    controller.findAll.map(vs => controller.toJson(vs.asJava))
  }

  get("/:uuid") {
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

  get("/name/:name") {
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

  get("/names") {
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

  get("/cameras") {
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

  get("/camera/:camera_id/:timestamp") {
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

  post("/") {
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

  delete("/:uuid") {
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

  put("/:uuid") {
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
