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

package org.mbari.vampiresquid.endpoints

import java.time.Instant
import java.util.UUID
import org.mbari.vampiresquid.controllers.VideoSequenceController
import org.mbari.vampiresquid.domain.LastUpdatedTime
import org.mbari.vampiresquid.domain.VideoSequence
import org.mbari.vampiresquid.domain.{BadRequest, ErrorMsg}
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.tapir.TapirCodecs
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import org.mbari.vampiresquid.domain.VideoSequenceCreate
import org.mbari.vampiresquid.domain.VideoSequenceUpdate

class VideoSequenceEndpoints(controller: VideoSequenceController)(using ec: ExecutionContext, jwtService: JwtService)
    extends Endpoints:

    // GET v1/videosequences
    val findAllVideoSequences: Endpoint[Unit, Paging, ErrorMsg, List[VideoSequence], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences")
            .in(paging)
            .out(jsonBody[List[VideoSequence]])
            .name("findAllVideoSequences")
            .description("Find all video sequences")
            .tag("video sequences")

    val findAllVideoSequencesImpl: ServerEndpoint[Any, Future] =
        findAllVideoSequences
            .serverLogic { page =>
                handleErrors(controller.findAll(page.offset.getOrElse(0), page.limit.getOrElse(100)))
            }

    // GET v1/videosequences/lastudpate/:uuid
    val findLastUpdateForVideoSequence: Endpoint[Unit, UUID, ErrorMsg, LastUpdatedTime, Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "lastupdate" / path[UUID]("uuid"))
            .out(jsonBody[LastUpdatedTime])
            .name("findLastUpdateForVideoSequence")
            .description("Find last update for a video sequence")
            .tag("video sequences")

    val findLastUpdateForVideoSequenceImpl: ServerEndpoint[Any, Future] =
        findLastUpdateForVideoSequence
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
    val findVideoSequenceByName: Endpoint[Unit, String, ErrorMsg, VideoSequence, Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "name" / path[String]("name"))
            .out(jsonBody[VideoSequence])
            .name("findVideoSequenceByName")
            .description("Find video sequences by name")
            .tag("video sequences")

    val findVideoSequenceByNameImpl: ServerEndpoint[Any, Future] =
        findVideoSequenceByName
            .serverLogic { req => handleOption(controller.findByName(req)) }

    // GET v1/videosequences/names
    val findAllVideoSequenceNames: Endpoint[Unit, Unit, ErrorMsg, List[String], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "names")
            .out(jsonBody[List[String]])
            .name("findAllVideoSequenceNames")
            .description("Find all video sequence names")
            .tag("video sequences")

    val findAllVideoSequenceNamesImpl: ServerEndpoint[Any, Future] =
        findAllVideoSequenceNames
            .serverLogic { _ => handleErrors(controller.findAllNames().map(_.toList)) }

    // GET v1/videosequences/camera/:cameraid
    val findVideoSequenceNamesByCameraId: Endpoint[Unit, String, ErrorMsg, List[String], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "names" / "camera" / path[String]("cameraid"))
            .out(jsonBody[List[String]])
            .name("findVideoSequenceNamesByCameraId")
            .description("Find video sequences by camera id")
            .tag("video sequences")

    val findVideoSequenceNamesByCameraIdImpl: ServerEndpoint[Any, Future] =
        findVideoSequenceNamesByCameraId
            .serverLogic { req => handleErrors(controller.findAllNamesByCameraID(req).map(_.toList)) }

    // GET v1/videosequences/cameras
    val findAllCameraIds: Endpoint[Unit, Unit, ErrorMsg, List[String], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "cameras")
            .out(jsonBody[List[String]])
            .name("findAllCameraIds")
            .description("Find all camera ids")
            .tag("video sequences")

    val findAllCameraIdsImpl: ServerEndpoint[Any, Future] =
        findAllCameraIds
            .serverLogic { _ => handleErrors(controller.findAllCameraIDs().map(_.toList)) }

    // GET v1/videosequences/camera/:cameraid
    val findVideoSequencesByCameraId: Endpoint[Unit, String, ErrorMsg, List[VideoSequence], Any] =
        openEndpoint
            .get
            .in("v1" / "videosequences" / "camera" / path[String]("cameraid"))
            .out(jsonBody[List[VideoSequence]])
            .name("findVideoSequencesByCameraId")
            .description("Find video sequences by camera id")
            .tag("video sequences")

    val findVideoSequencesByCameraIdImpl: ServerEndpoint[Any, Future] =
        findVideoSequencesByCameraId
            .serverLogic { req => handleErrors(controller.findByCameraId(req).map(_.toList)) }

    // GET v1/videosequences/camera/:cameraid/:timestamp
    val findVideoSequencesByCameraIdAndTimestamp
        : Endpoint[Unit, (String, Instant), ErrorMsg, List[VideoSequence], Any] =
        openEndpoint
            .get
            .in(
                "v1" / "videosequences" / "camera" / path[String]("cameraid") / path[Instant]("timestamp")(
                    TapirCodecs.instantCodec
                )
            )
            .out(jsonBody[List[VideoSequence]])
            .name("findVideoSequencesByCameraIdAndTimestamp")
            .description("Find video sequences by camera id and timestamp")
            .tag("video sequences")

    val findVideoSequencesByCameraIdAndTimestampImpl: ServerEndpoint[Any, Future] =
        findVideoSequencesByCameraIdAndTimestamp
            .serverLogic { (cameraId, timestamp) =>
                handleErrors(controller.findByCameraIDAndTimestamp(cameraId, timestamp).map(_.toList))
            }

    // POST v1/videosequences (form body)
    val createOneVideoSequence: Endpoint[Option[String], VideoSequenceCreate, ErrorMsg, VideoSequence, Any] =
        secureEndpoint
            .post
            .in("v1" / "videosequences")
            .in(oneOfBody(formBody[VideoSequenceCreate], jsonBody[VideoSequenceCreate]))
            .out(jsonBody[VideoSequence])
            .name("createOneVideoSequence")
            .description("Create a video sequence")
            .tag("video sequences")

    val createOneVideoSequenceImpl: ServerEndpoint[Any, Future] =
        createOneVideoSequence
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ => req => handleErrors(controller.create(req.name, req.camera_id, req.description)))

    // PUT v1/videosequences/:uuid (form body)
    val updateOneVideoSequence: Endpoint[Option[String], (UUID, VideoSequenceUpdate), ErrorMsg, VideoSequence, Any] =
        secureEndpoint
            .put
            .in("v1" / "videosequences" / path[UUID]("uuid"))
            .in(oneOfBody(formBody[VideoSequenceUpdate], jsonBody[VideoSequenceUpdate]))
            .out(jsonBody[VideoSequence])
            .name("updateOneVideoSequence")
            .description("Update a video sequence")
            .tag("video sequences")

    val updateOneVideoSequenceImpl: ServerEndpoint[Any, Future] =
        updateOneVideoSequence
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ =>
                (uuid, req) => handleErrors(controller.update(uuid, req.name, req.camera_id, req.description))
            )

    // DELETE v1/videosequences/:uuid
    val deleteOneVideoSequence: Endpoint[Option[String], UUID, ErrorMsg, Unit, Any] =
        secureEndpoint
            .delete
            .in("v1" / "videosequences" / path[UUID]("uuid"))
            .out(statusCode(StatusCode.NoContent).and(emptyOutput))
            .name("deleteOneVideoSequence")
            .description("Delete a video sequence by UUID")
            .tag("video sequences")

    val deleteOneVideoSequenceImpl: ServerEndpoint[Any, Future] =
        deleteOneVideoSequence
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ => req => handleErrors(controller.delete(req).map(b => if b then Right(()) else Left(()))))

    override def all: List[Endpoint[?, ?, ?, ?, ?]] = List(
        findVideoSequencesByCameraIdAndTimestamp,
        findVideoSequencesByCameraId,
        findAllCameraIds,
        findLastUpdateForVideoSequence,
        findVideoSequenceNamesByCameraId,
        findVideoSequenceByName,
        findAllVideoSequenceNames,
        createOneVideoSequence,
        updateOneVideoSequence,
        deleteOneVideoSequence,
        findAllVideoSequences
    )

    override def allImpl: List[ServerEndpoint[Any, Future]] = List(
        findVideoSequencesByCameraIdAndTimestampImpl,
        findVideoSequencesByCameraIdImpl,
        findAllCameraIdsImpl,
        findLastUpdateForVideoSequenceImpl,
        findVideoSequenceNamesByCameraIdImpl,
        findVideoSequenceByNameImpl,
        findAllVideoSequenceNamesImpl,
        createOneVideoSequenceImpl,
        updateOneVideoSequenceImpl,
        deleteOneVideoSequenceImpl,
        findAllVideoSequencesImpl
    )
