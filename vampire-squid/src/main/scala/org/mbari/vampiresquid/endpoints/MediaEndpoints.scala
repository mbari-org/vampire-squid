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

import java.net.URI
import java.net.URLDecoder
import java.time.Instant
import java.util.HexFormat
import java.util.UUID
import org.mbari.vampiresquid.controllers.MediaController
import org.mbari.vampiresquid.domain.{BadRequest, ErrorMsg, Media, NotFound, ServerError, Unauthorized}
import org.mbari.vampiresquid.domain.MoveVideoParams
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import org.mbari.vampiresquid.etc.jdk.Logging
import org.mbari.vampiresquid.etc.jdk.Logging.given
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.tapir.TapirCodecs
import org.mbari.vampiresquid.etc.tapir.TapirCodecs.given

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

class MediaEndpoints(mediaController: MediaController, jwtService: JwtService)(using ec: ExecutionContext)
    extends Endpoints:

    given givenJwtService: JwtService = jwtService
    private val log                   = Logging(getClass)
    private val hex                   = HexFormat.of()

    private def handleMediaOption(f: Future[Option[Media]])(using
        ec: ExecutionContext
    ): Future[Either[ErrorMsg, Media]] =
        f.transform:
            case Success(Some(m)) => scala.util.Success(Right(m))
            case Success(None)    => scala.util.Success(Left(NotFound("Media not found")))
            case Failure(e)       => scala.util.Success(Left(ServerError(e.getMessage)))

    // POST v1/media ----------------------------------------
    // val createMedia: Endpoint[Option[String], Map[String, String], ErrorMsg, Media, Any] =
    val createMedia: Endpoint[Option[String], Media, ErrorMsg, Media, Any] =
        secureEndpoint
            .post
            .in("v1" / "media")
            .in(oneOfBody(formBody[Media], jsonBody[Media]))
            .out(jsonBody[Media])
            .name("createMedia")
            .description("Create a new media.(The `uuid` param will be ignored if present.)")
            .tag("media")

    val createMediaImpl: ServerEndpoint[Any, Future] =
        createMedia
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ => media => handleErrors(mediaController.createMedia(media)))

    // PUT v1/media ----------------------------------------
    val updateMedia: Endpoint[Option[String], Media, ErrorMsg, Media, Any] =
        secureEndpoint
            .put
            .in("v1" / "media")
            .in(oneOfBody(formBody[Media], jsonBody[Media]))
            .out(jsonBody[Media])
            .name("updateMedia")
            .description("Update an existing media. (All uuid params will be ignored if present.)")
            .tag("media")

    val updateMediaImpl: ServerEndpoint[Any, Future]                                                   =
        updateMedia
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ => media => handleMediaOption(mediaController.updateMedia(media)))

        // PUT v1/media/{videoReferenceUuid} ----------------------------------------
    val updateMediaByVideoReferenceUuid: Endpoint[Option[String], (UUID, Media), ErrorMsg, Media, Any] =
        secureEndpoint
            .put
            .in("v1" / "media" / path[UUID]("videoReferenceUuid"))
            .in(oneOfBody(formBody[Media], jsonBody[Media]))
            .out(jsonBody[Media])
            .name("updateMediaByVideoReferenceUuid")
            .description("Update an existing media by videoReferenceUuid and form data")
            .tag("media")

    val updateMediaByVideoReferenceUuidImpl: ServerEndpoint[Any, Future] =
        updateMediaByVideoReferenceUuid
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ =>
                // (videoReferenceUuid, formMap) =>
                //     val media = Media.fromFormMap(formMap)
                //     log.atTrace.log("updateEndpointImpl received " + media)
                //     handleMediaOption(mediaController.findAndUpdateMedia(d => d.findByUUID(videoReferenceUuid), media))
                (videoReferenceUuid, media) =>
                    log.atTrace.log("updateEndpointImpl received " + media)
                    handleMediaOption(
                        mediaController
                            .findAndUpdateMedia(d => d.findByUUID(videoReferenceUuid), media)
                    )
            )

    // PUT v1/media/move/{videoReferenceUuid} w/ Form Body --------------------------
    val moveMediaByVideoReferenceUuid: Endpoint[Option[String], (UUID, MoveVideoParams), ErrorMsg, Media, Any] =
        secureEndpoint
            .put
            .in("v1" / "media" / "move" / path[UUID]("videoReferenceUuid"))
            .in(formBody[MoveVideoParams])
            .out(jsonBody[Media])
            .name("moveMediaByVideoReferenceUuid")
            .description("Move an existing media by videoReferenceUuid and form data")
            .tag("media")

    val moveMediaByVideoReferenceUuidImpl: ServerEndpoint[Any, Future] =
        moveMediaByVideoReferenceUuid
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ =>
                (videoReferenceUuid, moveVideoParams) =>
                    log.atTrace.log("moveByVideoReferenceUuidEndpoint received " + moveVideoParams)
                    handleMediaOption(
                        mediaController
                            .moveVideoReference(
                                videoReferenceUuid,
                                moveVideoParams.video_name,
                                moveVideoParams.start_timestamp,
                                moveVideoParams.duration
                            )
                    )
            )

    // GET v1/media/sha512/{hex encoded sha512} ------------------------------------
    val findMediaBySha512: Endpoint[Unit, String, ErrorMsg, Media, Any] =
        openEndpoint
            .get
            .in("v1" / "media" / "sha512" / path[String]("sha512"))
            .out(jsonBody[Media])
            .name("findMediaBySha512")
            .description("Find media by sha512")
            .tag("media")

    val findMediaBySha512Impl: ServerEndpoint[Any, Future] =
        findMediaBySha512
            .serverLogic((sha512: String) =>
                log.atTrace.log("findBySha512 received " + sha512)
                val bytes = hex.parseHex(sha512)
                handleMediaOption(mediaController.findBySha512(bytes))
            )

    // GET v1/media/videoreference/{videoReferenceUuid} --------------------------------
    val findMediaByVideoReferenceUuid: Endpoint[Unit, UUID, ErrorMsg, Media, Any] =
        openEndpoint
            .get
            .in("v1" / "media" / "videoreference" / path[UUID]("videoReferenceUuid"))
            .out(jsonBody[Media])
            .name("findMediaByVideoReferenceUuid")
            .description("Find media by videoReferenceUuid")
            .tag("media")

    val findMediaByVideoReferenceUuidImpl: ServerEndpoint[Any, Future] =
        findMediaByVideoReferenceUuid
            .serverLogic((videoReferenceUuid: UUID) =>
                log.atTrace.log("findByVideoReferenceUuid received " + videoReferenceUuid)
                handleMediaOption(mediaController.findByVideoReferenceUuid(videoReferenceUuid))
            )

    // GET v1/media/videoreference/filename/{filename} ---------------------------------
    val findMediaByFileName: Endpoint[Unit, String, ErrorMsg, List[Media], Any] =
        openEndpoint
            .get
            .in("v1" / "media" / "videoreference" / "filename" / path[String]("filename"))
            .out(jsonBody[List[Media]])
            .name("findMediaByFileName")
            .description("Find media by filename")
            .tag("media")

    val findMediaByFileNameImpl: ServerEndpoint[Any, Future] =
        findMediaByFileName
            .serverLogic((filename: String) =>
                log.atTrace.log("findByFileName received " + filename)
                handleErrors(mediaController.findByFileName(filename).map(_.toList))
            )

    // GET v1/media/videoreference/videosequence/{name} -----------------------
    val findMediaByVideoSequenceName: Endpoint[Unit, String, ErrorMsg, List[Media], Any] =
        openEndpoint
            .get
            .in("v1" / "media" / "videosequence" / path[String]("name"))
            .out(jsonBody[List[Media]])
            .name("findMediaByVideoSequenceName")
            .description("Find media by video sequence name")
            .tag("media")

    val findMediaByVideoSequenceNameImpl: ServerEndpoint[Any, Future] =
        findMediaByVideoSequenceName
            .serverLogic((videoSequenceName: String) =>
                log.atTrace.log("findByVideoSequenceName received " + videoSequenceName)
                handleErrors(mediaController.findByVideoSequenceName(videoSequenceName).map(_.toList))
            )

    // POST v1/media/videoreference/videosequence -----------------------
    val findMediaByVideoSequenceNames: Endpoint[Unit, (Paging, Seq[String]), ErrorMsg, List[Media], Any] =
        openEndpoint
            .post
            .in("v1" / "media" / "videosequence")
            .in(paging)
            .in(jsonBody[Seq[String]])
            .out(jsonBody[List[Media]])
            .name("findMediaByVideoSequenceNames")
            .description("Find media by a list of video sequence names")
            .tag("media")

    val findMediaByVideoSequenceNamesImpl: ServerEndpoint[Any, Future] =
        findMediaByVideoSequenceNames
            .serverLogic((paging: Paging, videoSequenceNames: Seq[String]) =>
                log.atTrace.log("findByVideoSequenceNames received " + videoSequenceNames)
                handleErrors(mediaController.findByVideoSequenceNames(videoSequenceNames, paging.offset, paging.limit).map(_.toList))
            )

    // GET v1/media/video/{name} ---------------------------------------------
    val findMediaByVideoName: Endpoint[Unit, String, ErrorMsg, List[Media], Any] =
        openEndpoint
            .get
            .in("v1" / "media" / "video" / path[String]("name"))
            .out(jsonBody[List[Media]])
            .name("findMediaByVideoName")
            .description("Find media by video name")
            .tag("media")

    val findMediaByVideoNameImpl: ServerEndpoint[Any, Future] =
        findMediaByVideoName
            .serverLogic((videoName: String) =>
                log.atTrace.log("findByVideoName received " + videoName)
                handleErrors(mediaController.findByVideoName(videoName).map(_.toList))
            )

    // GET v1/media/camera/{cameraId}/{startTimestamp}/{endTimestamp} ----------------
    val findMediaByCameraIdAndTimestamps: Endpoint[Unit, (String, Instant, Instant), ErrorMsg, List[Media], Any] =
        openEndpoint
            .get
            .in(
                "v1" / "media" / "camera" / path[String]("cameraId") / path[Instant]("startTimestamp")(
                    TapirCodecs.instantCodec
                ) / path[Instant](
                    "endTimestamp"
                )(TapirCodecs.instantCodec)
            )
            .out(jsonBody[List[Media]])
            .name("findMediaByCameraIdAndTimestamps")
            .description("Find media by cameraId and timestamps")
            .tag("media")

    val findMediaByCameraIdAndTimestampsImpl: ServerEndpoint[Any, Future] =
        findMediaByCameraIdAndTimestamps
            .serverLogic((cameraId: String, startTimestamp: Instant, endTimestamp: Instant) =>
                log.atTrace
                    .log("findByCameraIdAndTimestamps received " + cameraId + " " + startTimestamp + " " + endTimestamp)
                handleErrors(
                    mediaController
                        .findByCameraIdAndTimestamps(cameraId, startTimestamp, endTimestamp)
                        .map(_.toList)
                )
            )

    // GET v1/media/concurrent/{videoReferenceUuid} -----------------------------------
    val findConcurrentMediaByVideoReferenceUuid: Endpoint[Unit, UUID, ErrorMsg, List[Media], Any] =
        openEndpoint
            .get
            .in("v1" / "media" / "concurrent" / path[UUID]("videoReferenceUuid"))
            .out(jsonBody[List[Media]])
            .name("findConcurrentMediaByVideoReferenceUuid")
            .description("Find concurrent media by videoReferenceUuid")
            .tag("media")

    val findConcurrentMediaByVideoReferenceUuidImpl: ServerEndpoint[Any, Future] =
        findConcurrentMediaByVideoReferenceUuid
            .serverLogic((videoReferenceUuid: UUID) =>
                log.atTrace.log("findConcurrentByVideoReferenceUuid received " + videoReferenceUuid)
                handleErrors(
                    mediaController
                        .findConcurrent(videoReferenceUuid)
                        .map(_.toList)
                )
            )

    // GET v1/media/camera/{cameraId}/{datetime} --------------------------------------
    val findMediaByCameraIdAndDatetime: Endpoint[Unit, (String, Instant), ErrorMsg, List[Media], Any] =
        openEndpoint
            .get
            .in(
                "v1" / "media" / "camera" / path[String]("cameraId") / path[Instant]("datetime")(
                    TapirCodecs.instantCodec
                )
            )
            .out(jsonBody[List[Media]])
            .name("findMediaByCameraIdAndDatetime")
            .description("Find media by cameraId and datetime")
            .tag("media")

    val findMediaByCameraIdAndDatetimeImpl: ServerEndpoint[Any, Future] =
        findMediaByCameraIdAndDatetime
            .serverLogic((cameraId: String, datetime: Instant) =>
                log.atTrace.log("findByCameraIdAndDatetime received " + cameraId + " " + datetime)
                handleErrors(
                    mediaController
                        .findByCameraIdAndTimestamp(cameraId, datetime)
                        .map(_.toList)
                )
            )

    // GET v1/media/uri/{uri} ---------------------------------------------------------
    val findMediaByUri: Endpoint[Unit, String, ErrorMsg, List[Media], Any] =
        openEndpoint
            .get
            .in("v1" / "media" / "uri" / path[String]("uri"))
            .out(jsonBody[List[Media]])
            .name("findMediaByUri")
            .description("Find media by uri")
            .tag("media")

    val findMediaByUriImpl: ServerEndpoint[Any, Future] =
        findMediaByUri
            .serverLogic((uri: String) =>
                log.atTrace.log("findByUri received " + uri)
                Try(URLDecoder.decode(uri, "UTF-8"))
                    .map(URI.create)
                    .toEither match
                    case Left(_)         => Future.successful(Left(BadRequest(s"Invalid URI: $uri")))
                    case Right(validUri) =>
                        handleErrors(
                            mediaController
                                .findByURI(validUri)
                                .map(_.toList)
                        )
            )

    override def all: List[Endpoint[?, ?, ?, ?, ?]] = List(
        createMedia,
        updateMedia,
        updateMediaByVideoReferenceUuid,
        moveMediaByVideoReferenceUuid,
        findConcurrentMediaByVideoReferenceUuid,
        findMediaByCameraIdAndDatetime,
        findMediaByCameraIdAndTimestamps,
        findMediaByFileName,
        findMediaBySha512,
        findMediaByUri,
        findMediaByVideoName,
        findMediaByVideoReferenceUuid,
        findMediaByVideoSequenceName
    )

    override def allImpl: List[ServerEndpoint[Any, concurrent.Future]] = List(
        createMediaImpl,
        updateMediaImpl,
        updateMediaByVideoReferenceUuidImpl,
        moveMediaByVideoReferenceUuidImpl,
        findConcurrentMediaByVideoReferenceUuidImpl,
        findMediaByCameraIdAndDatetimeImpl,
        findMediaByCameraIdAndTimestampsImpl,
        findMediaByFileNameImpl,
        findMediaBySha512Impl,
        findMediaByUriImpl,
        findMediaByVideoNameImpl,
        findMediaByVideoReferenceUuidImpl,
        findMediaByVideoSequenceNameImpl
    )
