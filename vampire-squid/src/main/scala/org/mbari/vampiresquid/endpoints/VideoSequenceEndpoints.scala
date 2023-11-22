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

package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.controllers.VideoSequenceController
import org.mbari.vampiresquid.etc.jwt.JwtService
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import org.mbari.vampiresquid.domain.VideoSequence
import org.mbari.vampiresquid.domain.{BadRequest, ErrorMsg}
import scala.concurrent.ExecutionContext
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import java.util.UUID
import scala.concurrent.Future
import sttp.tapir.server.ServerEndpoint
import java.time.Instant
import org.mbari.vampiresquid.domain.LastUpdatedTime
import sttp.model.StatusCode

class VideoSequenceEndpoints(controller: VideoSequenceController)(using ec: ExecutionContext, jwtService: JwtService)
    extends Endpoints:

    // GET v1/videosequences
    val findAllEndpoint: Endpoint[Unit, Unit, ErrorMsg, List[VideoSequence], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences")
            .out(jsonBody[List[VideoSequence]])
            .name("findAll")
            .description("Find all video sequences")
            .tag("video sequences")

    val findAllEndpointImpl: ServerEndpoint[Any, Future] =
        findAllEndpoint
            .serverLogic { _ => handleErrors(controller.findAll()) }

    // GET v1/videosequences/lastudpate/:uuid
    val findLastUpdateEndpoint: Endpoint[Unit, UUID, ErrorMsg, LastUpdatedTime, Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "lastupdate" / path[UUID]("uuid"))
            .out(jsonBody[LastUpdatedTime])
            .name("findLastUpdate")
            .description("Find last update for a video sequence")
            .tag("video sequences")

    val findLastUpdateEndpointImpl: ServerEndpoint[Any, Future] =
        findLastUpdateEndpoint
            .serverLogic { req =>
                handleOption(
                    controller
                        .findByUUID(req)
                        .map(opt =>
                            for
                                vs  <- opt
                                lut <- vs.last_updated_time
                            yield LastUpdatedTime(lut)
                        )
                )
            }

    // GET v1/videosequences/name/:name
    val findByNameEndpoint: Endpoint[Unit, String, ErrorMsg, VideoSequence, Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "name" / path[String]("name"))
            .out(jsonBody[VideoSequence])
            .name("findByName")
            .description("Find video sequences by name")
            .tag("video sequences")

    val findByNameEndpointImpl: ServerEndpoint[Any, Future] =
        findByNameEndpoint
            .serverLogic { req => handleOption(controller.findByName(req)) }

    // GET v1/videosequences/names
    val findAllNamesEndpoint: Endpoint[Unit, Unit, ErrorMsg, List[String], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "names")
            .out(jsonBody[List[String]])
            .name("findAllNames")
            .description("Find all video sequence names")
            .tag("video sequences")

    val findAllNamesEndpointImpl: ServerEndpoint[Any, Future] =
        findAllNamesEndpoint
            .serverLogic { _ => handleErrors(controller.findAllNames().map(_.toList)) }

    // GET v1/videosequences/camera/:cameraid
    val findNamesByCameraIdEndpoint: Endpoint[Unit, String, ErrorMsg, List[String], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "names" / "camera" / path[String]("cameraid"))
            .out(jsonBody[List[String]])
            .name("findNamesByCameraId")
            .description("Find video sequences by camera id")
            .tag("video sequences")

    val findNamesByCameraIdEndpointImpl: ServerEndpoint[Any, Future] =
        findNamesByCameraIdEndpoint
            .serverLogic { req => handleErrors(controller.findAllNamesByCameraID(req).map(_.toList)) }

    // GET v1/videosequences/cameras
    val findAllCameraIdsEndpoint: Endpoint[Unit, Unit, ErrorMsg, List[String], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "cameras")
            .out(jsonBody[List[String]])
            .name("findAllCameraIds")
            .description("Find all camera ids")
            .tag("video sequences")

    val findAllCameraIdsEndpointImpl: ServerEndpoint[Any, Future] =
        findAllCameraIdsEndpoint
            .serverLogic { _ => handleErrors(controller.findAllCameraIDs().map(_.toList)) }

    // GET v1/videosequences/camera/:cameraid
    val findByCameraIdEndpoint: Endpoint[Unit, String, ErrorMsg, List[VideoSequence], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "camera" / path[String]("cameraid"))
            .out(jsonBody[List[VideoSequence]])
            .name("findByCameraId")
            .description("Find video sequences by camera id")
            .tag("video sequences")

    val findByCameraIdEndpointImpl: ServerEndpoint[Any, Future] =
        findByCameraIdEndpoint
            .serverLogic { req => handleErrors(controller.findByCameraId(req).map(_.toList)) }

    // GET v1/videosequences/camera/:cameraid/:timestamp
    val findByCameraIdAndTimestampEndpoint: Endpoint[Unit, (String, Instant), ErrorMsg, List[VideoSequence], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "camera" / path[String]("cameraid") / path[Instant]("timestamp"))
            .out(jsonBody[List[VideoSequence]])
            .name("findByCameraIdAndTimestamp")
            .description("Find video sequences by camera id and timestamp")
            .tag("video sequences")

    val findByCameraIdAndTimestampEndpointImpl: ServerEndpoint[Any, Future] =
        findByCameraIdAndTimestampEndpoint
            .serverLogic { (cameraId, timestamp) =>
                handleErrors(controller.findByCameraIDAndTimestamp(cameraId, timestamp).map(_.toList))
            }

    // POST v1/videosequences (form body)
    val createEndpoint: Endpoint[Option[String], Map[String, String], ErrorMsg, VideoSequence, Any] =
        secureEndpoint
            .post
            .in("v1" / "videosequences")
            .in(formBody[Map[String, String]])
            .out(jsonBody[VideoSequence])
            .name("create")
            .description("Create a video sequence")
            .tag("video sequences")

    val createEndpointImpl: ServerEndpoint[Any, Future] =
        createEndpoint
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ =>
                req =>
                    val name     = req.get("name")
                    val cameraId = req.get("camera_id")
                    if name.isEmpty || cameraId.isEmpty then
                        Future.successful(Left(BadRequest("Missing name or camera_id")))
                    else {
                        val description = req.get("description")
                        handleErrors(controller.create(name.get, cameraId.get, description))
                    }
            )

    // PUT v1/videosequences/:uuid (form body)
    val updateEndpoint: Endpoint[Option[String], (UUID, Map[String, String]), ErrorMsg, VideoSequence, Any] =
        secureEndpoint
            .put
            .in("v1" / "videosequences" / path[UUID]("uuid"))
            .in(formBody[Map[String, String]])
            .out(jsonBody[VideoSequence])
            .name("update")
            .description("Update a video sequence")
            .tag("video sequences")

    val updateEndpointImpl: ServerEndpoint[Any, Future] =
        updateEndpoint
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ =>
                (uuid, form) =>
                    val name        = form.get("name")
                    val cameraId    = form.get("camera_id")
                    val description = form.get("description")
                    handleErrors(controller.update(uuid, name, cameraId, description))
            )

    // DELETE v1/videosequences/:uuid
    val deleteEndpoint: Endpoint[Option[String], UUID, ErrorMsg, Unit, Any] =
        secureEndpoint
            .delete
            .in("v1" / "videosequences" / path[UUID]("uuid"))
            .out(statusCode(StatusCode.NoContent).and(emptyOutput))
            .name("deleteByUuid")
            .description("Delete a video sequence by UUID")
            .tag("video sequence")

    val deleteEndpointImpl: ServerEndpoint[Any, Future] =
        deleteEndpoint
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ => req => handleErrors(controller.delete(req).map(b => if b then Right(()) else Left(()))))

    override def all: List[Endpoint[?, ?, ?, ?, ?]] = List(
        findAllEndpoint,
        findLastUpdateEndpoint,
        findByNameEndpoint,
        findAllNamesEndpoint,
        findNamesByCameraIdEndpoint,
        findAllCameraIdsEndpoint,
        findByCameraIdEndpoint,
        findByCameraIdAndTimestampEndpoint,
        createEndpoint,
        updateEndpoint,
        deleteEndpoint
    )

    override def allImpl: List[ServerEndpoint[Any, Future]] = List(
        findAllEndpointImpl,
        findLastUpdateEndpointImpl,
        findByNameEndpointImpl,
        findAllNamesEndpointImpl,
        findNamesByCameraIdEndpointImpl,
        findAllCameraIdsEndpointImpl,
        findByCameraIdEndpointImpl,
        findByCameraIdAndTimestampEndpointImpl,
        createEndpointImpl,
        updateEndpointImpl,
        deleteEndpointImpl
    )
