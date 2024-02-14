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

import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.mbari.vampiresquid.controllers.VideoController
import org.mbari.vampiresquid.controllers.VideoSequenceController
import org.mbari.vampiresquid.domain.BadRequest
import org.mbari.vampiresquid.domain.ErrorMsg
import org.mbari.vampiresquid.domain.LastUpdatedTime
import org.mbari.vampiresquid.domain.Video
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.tapir.TapirCodecs

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.server.ServerEndpoint
import sttp.model.StatusCode
import org.mbari.vampiresquid.Endpoints.videoController

import CustomTapirJsonCirce.*

class VideoEndpoints(controller: VideoController, videoSequenceController: VideoSequenceController)(using
    ec: ExecutionContext,
    jwtService: JwtService
) extends Endpoints:

    // GET v1/videos
    val findAllVideos: Endpoint[Unit, Paging, ErrorMsg, List[Video], Any] =
        openEndpoint
            .get
            .in("v1" / "videos")
            .in(paging)
            .out(jsonBody[List[Video]])
            .name("findAllVideos")
            .description("Find all videos")
            .tag("videos")
    // TODO add limit and offset params

    val findAllVideosImpl: ServerEndpoint[Any, Future] =
        findAllVideos
            .serverLogic(page =>
                handleErrors(controller.findAll(page.offset.getOrElse(0), page.limit.getOrElse(100)).map(_.toList))
            )

    // GET v1/videos/:uuid
    val findOneVideo: Endpoint[Unit, UUID, ErrorMsg, Video, Any] =
        openEndpoint
            .get
            .in("v1" / "videos" / path[UUID]("uuid"))
            .out(jsonBody[Video])
            .name("findOneVideo")
            .description("Find a video by UUID")
            .tag("videos")

    val findOneVideoImpl: ServerEndpoint[Any, Future] =
        findOneVideo
            .serverLogic(req => handleOption(controller.findByUUID(req)))

    // GET v1/videos/videosequence/:uuid
    val findVideoByVideoSequenceUuid: Endpoint[Unit, UUID, ErrorMsg, List[Video], Any] =
        openEndpoint
            .get
            .in("v1" / "videos" / "videosequence" / path[UUID]("videoSequenceUuid"))
            .out(jsonBody[List[Video]])
            .name("findVideoByVideoSequenceUuid")
            .description("Find a videos by its video sequence UUID")
            .tag("videos")

    val findVideoByVideoSequenceUuidImpl: ServerEndpoint[Any, Future] =
        findVideoByVideoSequenceUuid
            .serverLogic(req => handleErrors(controller.findByVideoSequenceUUID(req).map(_.toList)))

    // GET v1/videos/videoreference/:uuid/
    val findVideoByVideoReferenceUuid: Endpoint[Unit, UUID, ErrorMsg, List[Video], Any] =
        openEndpoint
            .get
            .in("v1" / "videos" / "videoreference" / path[UUID]("videoReferenceUuid"))
            .out(jsonBody[List[Video]])
            .name("findVideoByVideoReferenceUuid")
            .description("Find a videos by its video reference UUID")
            .tag("videos")

    val findVideoByVideoReferenceUuidImpl: ServerEndpoint[Any, Future] =
        findVideoByVideoReferenceUuid
            .serverLogic(req => handleErrors(controller.findByVideoReferenceUUID(req).map(_.toList)))

    // get v1/videos/lastupdate/:uuid
    val findLastUpdateForVideo: Endpoint[Unit, UUID, ErrorMsg, LastUpdatedTime, Any] =
        openEndpoint
            .get
            .in("v1" / "videos" / "lastupdate" / path[UUID]("uuid"))
            .out(jsonBody[LastUpdatedTime])
            .name("findLastUpdateForVideo")
            .description("Find the last update time for a video by UUID")
            .tag("videos")

    val findLastUpdateForVideoImpl: ServerEndpoint[Any, Future] =
        findLastUpdateForVideo
            .serverLogic(req =>
                handleOption(
                    controller
                        .findByUUID(req)
                        .map(opt =>
                            for
                                video <- opt
                                lut   <- video.last_updated_time
                            yield LastUpdatedTime(lut)
                        )
                )
            )

    // GET v1/videos/name/:name
    val findVideoByName: Endpoint[Unit, String, ErrorMsg, List[Video], Any] =
        openEndpoint
            .get
            .in("v1" / "videos" / "name" / path[String]("name"))
            .out(jsonBody[List[Video]])
            .name("findVideoByName")
            .description("Find a videos by its name")
            .tag("videos")

    val findVideoByNameImpl: ServerEndpoint[Any, Future] =
        findVideoByName
            .serverLogic(req => handleErrors(controller.findByName(req).map(_.toList)))

    // GET v1/videosequence/:name
    val findVideoByVideoSequenceName: Endpoint[Unit, String, ErrorMsg, List[Video], Any] =
        openEndpoint
            .get
            .in("v1" / "videos" / "videosequence" / "name" / path[String]("videoSequenceName"))
            .out(jsonBody[List[Video]])
            .name("findVideoByVideoSequenceByName")
            .description("Find videos by its video sequence name")
            .tag("videos")

    val findVideoByVideoSequenceByNameImpl: ServerEndpoint[Any, Future] =
        findVideoByVideoSequenceName
            .serverLogic(req =>
                val future = for
                    vs <- videoSequenceController.findByName(req)
                    if vs.isDefined
                    xx <- controller.findByVideoSequenceUUID(vs.get.uuid)
                yield xx.toList

                handleErrors(future)
            )

    // GET v1/videos/names/videosequence/:name
    val findVideoNamesByVideoSequenceName = 
        openEndpoint
            .get
            .in("v1" / "videos" / "names" / "videosequence" / path[String]("videoSequenceName"))
            .out(jsonBody[Seq[String]])
            .name("findVideoNamesByVideoSequenceName")
            .description("Find video names by its video sequence name")
            .tag("videos")

    val findVideoNamesByVideoSequenceNameImpl: ServerEndpoint[Any, Future] =
        findVideoNamesByVideoSequenceName
            .serverLogic(req =>
                val future = videoController.findNamesByVideoSequenceName(req).map(_.toSeq)
                handleErrors(future)
            )

    // GET v1/videos/timestamp/:timestamp
    val findVideoByTimestamp: Endpoint[Unit, Instant, ErrorMsg, List[Video], Any] =
        openEndpoint
            .get
            .in("v1" / "videos" / "timestamp" / path[Instant]("timestamp")(TapirCodecs.instantCodec))
            .out(jsonBody[List[Video]])
            .name("findVideoByTimestamp")
            .description("Find videos by its timestamp")
            .tag("videos")

    val findVideoByTimestampImpl: ServerEndpoint[Any, Future] =
        findVideoByTimestamp
            .serverLogic(req => handleErrors(controller.findByTimestamp(req).map(_.toList)))

    // GET v1/videos/timestamp/:starthttps://tapir.softwaremill.com/en/latest/examples.html/:end
    val findVideoByTimestampRange: Endpoint[Unit, (Instant, Instant), ErrorMsg, List[Video], Any] =
        openEndpoint
            .get
            .in(
                "v1" / "videos" / "timestamp" / path[Instant]("start")(TapirCodecs.instantCodec) / path[Instant]("end")(
                    TapirCodecs.instantCodec
                )
            )
            .out(jsonBody[List[Video]])
            .name("findVideoByTimestampRange")
            .description("Find videos by its timestamp range")
            .tag("videos")

    val findVideoByTimestampRangeImpl: ServerEndpoint[Any, Future]                          =
        findVideoByTimestampRange
            .serverLogic((startTime, endTime) =>
                handleErrors(controller.findBetweenTimestamps(startTime, endTime).map(_.toList))
            )

        // POST v1/videos (form body)
    val createOneVideo: Endpoint[Option[String], Map[String, String], ErrorMsg, Video, Any] =
        secureEndpoint
            .post
            .in("v1" / "videos")
            .in(formBody[Map[String, String]])
            .out(jsonBody[Video])
            .name("createOneVideo")
            .description(
                "Create a video. Required form fields: name, video_sequence_uuid, start_timestamp (or start), duration_millis. Optional fields: description"
            )
            .tag("videos")

    val createOneVideoImpl: ServerEndpoint[Any, Future] =
        createOneVideo
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ =>
                req =>
                    val videoName         = req.get("name")
                    val videoSequenceUuid = req.get("video_sequence_uuid").map(UUID.fromString)
                    val start             = req.get("start").orElse(req.get("start_timestamp")).map(Instant.parse)
                    val duration          = req
                        .get("duration_millis")
                        .map(_.toLong)
                        .map(Duration.ofMillis)
                    val description       = req.get("description")

                    if videoName.isEmpty || videoSequenceUuid.isEmpty || start.isEmpty then
                        Future(
                            Left(
                                BadRequest(
                                    "Missing one or more required parameters: name, video_sequence_name, start_timestamp, duration_millis"
                                )
                            )
                        )
                    else
                        handleErrors(
                            controller.create(videoSequenceUuid.get, videoName.get, start.get, duration, description)
                        )
            )

    // DELETE v1/videos/:uuid
    val deleteVideoByUuid: Endpoint[Option[String], UUID, ErrorMsg, Unit, Any] =
        secureEndpoint
            .delete
            .in("v1" / "videos" / path[UUID]("uuid"))
            .out(statusCode(StatusCode.NoContent).and(emptyOutput))
            .name("deleteVideoByUuid")
            .description("Delete a video by UUID")
            .tag("videos")

    val deleteVideoByUuidImpl: ServerEndpoint[Any, Future]                                       =
        deleteVideoByUuid
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ => req => handleErrors(controller.delete(req).map(b => if b then Right(()) else Left(()))))

        // PUT v1/videos/:uuid (form body)
    val updateVideo: Endpoint[Option[String], (UUID, Map[String, String]), ErrorMsg, Video, Any] =
        secureEndpoint
            .put
            .in("v1" / "videos" / path[UUID]("uuid"))
            .in(formBody[Map[String, String]])
            .out(jsonBody[Video])
            .name("updateVideo")
            .description(
                "Update a video by UUID. Updateable fields: name, video_sequence_uuid, start_timestamp (or start), duration_millis, description"
            )
            .tag("videos")

    val updateVideoImpl: ServerEndpoint[Any, Future] =
        updateVideo
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ =>
                (videoUuid, formData) =>
                    val videoName         = formData.get("name")
                    val videoSequenceUuid = formData.get("video_sequence_uuid").map(UUID.fromString)
                    val start             = formData.get("start").orElse(formData.get("start_timestamp")).map(Instant.parse)
                    val duration          = formData
                        .get("duration_millis")
                        .map(_.toLong)
                        .map(Duration.ofMillis)
                    val description       = formData.get("description")

                    handleErrors(
                        controller.update(videoUuid, videoName, start, duration, description, videoSequenceUuid)
                    )
            )

    override def all: List[Endpoint[?, ?, ?, ?, ?]] = List(
        
        
        findVideoByVideoSequenceName,
        findVideoByVideoSequenceUuid,
        findVideoByVideoReferenceUuid,
        findLastUpdateForVideo,
        findVideoByName,
        findVideoNamesByVideoSequenceName,
        findVideoByTimestampRange,
        findVideoByTimestamp,
        findAllVideos,
        findOneVideo,
        createOneVideo,
        deleteVideoByUuid,
        updateVideo
    )

    override def allImpl: List[ServerEndpoint[Any, Future]] = List(
        
        
        findVideoByVideoSequenceByNameImpl,
        findVideoByVideoSequenceUuidImpl,
        findVideoByVideoReferenceUuidImpl,
        findLastUpdateForVideoImpl,
        findVideoByNameImpl,
        findVideoNamesByVideoSequenceNameImpl,
        findVideoByTimestampRangeImpl,
        findVideoByTimestampImpl,
        findAllVideosImpl,
        findOneVideoImpl,
        createOneVideoImpl,
        deleteVideoByUuidImpl,
        updateVideoImpl
    )
