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

package org.mbari.vars.vam.endpoints


import java.net.URI
import java.nio.charset.StandardCharsets
import org.glassfish.jaxb.core.api.impl.NameConverter.Standard
import org.mbari.vars.vam.auth.Authorization
import org.mbari.vars.vam.auth.AuthorizationService
import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.domain.{BadRequest, ErrorMsg, Media, NotFound, ServerError, Unauthorized}
import org.mbari.vars.vam.etc.circe.CirceCodecs.given
import org.mbari.vars.vam.etc.jwt.JwtService
import org.mbari.vars.vam.etc.sdk.Reflect

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import org.mbari.vars.vam.etc.jdk.Logging
import org.mbari.vars.vam.etc.jdk.Logging.given

import scala.util.control.NonFatal
import java.util.UUID
import org.mbari.vars.vam.domain.MoveVideoParams
import org.mbari.vars.vam.util.HexUtil
import org.mbari.vars.vam.model.MutableMedia
import scala.util.Success
import scala.util.Failure
import java.time.Instant
import java.net.URLDecoder
import scala.util.Try


class MediaEndpoints(mediaController: MediaController, jwtService: JwtService)(using ec: ExecutionContext) extends Endpoints {
  
  given Schema[Option[URI]] = Schema.string
  given Schema[Media] = Schema.derived[Media]
  given Schema[MoveVideoParams] = Schema.derived[MoveVideoParams]
  given givenJwtService: JwtService = jwtService
  private val log = Logging(getClass)

  private def handleMediaOption(f: Future[Option[MutableMedia]])(using ec: ExecutionContext): Future[Either[ErrorMsg, Media]] = 
    f.transform {
      case Success(Some(m)) => scala.util.Success(Right(Media.from(m)))
      case Success(None) => scala.util.Success(Left(NotFound("Media not found")))
      case Failure(e) => scala.util.Success(Left(ServerError(e.getMessage)))
    }

  // POST v1/media ----------------------------------------
  val createEndpoint: Endpoint[Option[String], Map[String, String], ErrorMsg, Media, Any] = 
    secureEndpoint
      .post
      .in("v1" / "media")
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .in(formBody[Map[String, String]])
      .out(jsonBody[Media])
      .name("create")
      .description("Create a new media")
      .tag("media")
      

  val createEndpointImpl: ServerEndpoint[Any, Future] =
    createEndpoint
      .serverSecurityLogic(jwtOpt => verify(jwtOpt))
      .serverLogic(_ => formMap => {
        val media = Media.fromFormMap(formMap)
        log.atTrace.log("createEndpointImpl received " + media)
        handleErrors(mediaController.create(media)
          .map(m => Media.from(m)))
      })

  // PUT v1/media ----------------------------------------
  val updateEndpoint: Endpoint[Option[String], Map[String, String], ErrorMsg, Media, Any] = 
    secureEndpoint
      .put
      .in("v1" / "media")
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .in(formBody[Map[String, String]])
      .out(jsonBody[Media])
      .name("update")
      .description("Update an existing media")
      .tag("media")

  val updateEndpointImpl: ServerEndpoint[Any, Future] =
    updateEndpoint
      .serverSecurityLogic(jwtOpt => verify(jwtOpt))
      .serverLogic(_ => formMap => {
        val media = Media.fromFormMap(formMap)
        val mutableMedia = Media.toMutableMedia(media)
        log.atTrace.log("updateEndpointImpl received " + media)
        handleMediaOption(mediaController.updateMedia(mutableMedia))
      })

    // PUT v1/media/{videoReferenceUuid} ----------------------------------------
  val updateByVideoReferenceUuidEndpoint: Endpoint[Option[String], (UUID, Map[String, String]), ErrorMsg, Media, Any] =
      secureEndpoint
        .put
        .in("v1" / "media" / path[UUID]("videoReferenceUuid"))
        .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
        .in(formBody[Map[String, String]])
        .out(jsonBody[Media])
        .name("update by videoReferenceUuid")
        .description("Update an existing media by videoReferenceUuid and form data")
        .tag("media")

  val updateByVideoReferenceUuidEndpointImpl: ServerEndpoint[Any, Future] =
    updateByVideoReferenceUuidEndpoint
      .serverSecurityLogic(jwtOpt => verify(jwtOpt))
      .serverLogic(_ => (videoReferenceUuid, formMap) => {
        val media = Media.fromFormMap(formMap)
        val mutableMedia = Media.toMutableMedia(media)
        log.atTrace.log("updateEndpointImpl received " + media)
        handleMediaOption(mediaController.findAndUpdateMedia(d => d.findByUUID(videoReferenceUuid), mutableMedia))
      })

  // PUT v1/media/move/{videoReferenceUuid} w/ Form Body --------------------------
  val moveByVideoReferenceUuidEndpoint: Endpoint[Option[String], (UUID, MoveVideoParams), ErrorMsg, Media, Any] =
    secureEndpoint
      .put
      .in("v1" / "media" / "move" / path[UUID]("videoReferenceUuid"))
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .in(formBody[MoveVideoParams])
      .out(jsonBody[Media])
      .name("move by videoReferenceUuid")
      .description("Move an existing media by videoReferenceUuid and form data")
      .tag("media")


  val moveByVideoReferenceUuidEndpointImpl: ServerEndpoint[Any, Future] =
    moveByVideoReferenceUuidEndpoint
      .serverSecurityLogic(jwtOpt => verify(jwtOpt))
      .serverLogic(_ => (videoReferenceUuid, moveVideoParams) => {
        log.atTrace.log("moveByVideoReferenceUuidEndpoint received " + moveVideoParams)
        handleMediaOption(mediaController.moveVideoReference(videoReferenceUuid, moveVideoParams.video_name, moveVideoParams.start_timestamp, moveVideoParams.duration))
      })

  // GET v1/media/sha512/{hex encoded sha512} ------------------------------------
  val findBySha512: Endpoint[Unit, String, ErrorMsg, Media, Any] =
    openEndpoint
      .get
      .in("v1" / "media" / "sha512" / path[String]("sha512"))
      .out(jsonBody[Media])
      .name("findBySha512")
      .description("Find media by sha512")
      .tag("media")

  val findBySha512Impl: ServerEndpoint[Any, Future] =
    findBySha512
      .serverLogic((sha512: String) => {
        log.atTrace.log("findBySha512 received " + sha512)
        val bytes = HexUtil.fromHex(sha512)
        handleMediaOption(mediaController.findBySha512(bytes))
      })

  // GET v1/media/videoreference/{videoReferenceUuid} --------------------------------
  val findByVideoReferenceUuid: Endpoint[Unit, UUID, ErrorMsg, Media, Any] =
    openEndpoint
      .get
      .in("v1" / "media" / "videoreference" / path[UUID]("videoReferenceUuid"))
      .out(jsonBody[Media])
      .name("findByVideoReferenceUuid")
      .description("Find media by videoReferenceUuid")
      .tag("media")

  val findByVideoReferenceUuidImpl: ServerEndpoint[Any, Future] =
    findByVideoReferenceUuid
      .serverLogic((videoReferenceUuid: UUID) => {
        log.atTrace.log("findByVideoReferenceUuid received " + videoReferenceUuid)
        handleMediaOption(mediaController.findByVideoReferenceUuid(videoReferenceUuid))
      })

  // GET v1/media/videoreference/filename/{filename} ---------------------------------
  val findByFileName: Endpoint[Unit, String, ErrorMsg, List[Media], Any] = 
    openEndpoint
      .get
      .in("v1" / "media" / "videoreference" / "filename" / path[String]("filename"))
      .out(jsonBody[List[Media]])
      .name("findByFileName")
      .description("Find media by filename")
      .tag("media")

  val findByFileNameImpl: ServerEndpoint[Any, Future] =
    findByFileName
      .serverLogic((filename: String) => {
        log.atTrace.log("findByFileName received " + filename)
        handleErrors(mediaController.findByFileName(filename)
          .map(_.map(Media.from)
          .toList))
      })

  // GET v1/media/videoreference/videosequence/{name} -----------------------
  val findByVideoSequenceName: Endpoint[Unit, String, ErrorMsg, List[Media], Any] = 
    openEndpoint
      .get
      .in("v1" / "media" / "videoreference" / "videosequence" / path[String]("name"))
      .out(jsonBody[List[Media]])
      .name("findByVideoSequenceName")
      .description("Find media by video sequence name")
      .tag("media")

  val findByVideoSequenceNameImpl: ServerEndpoint[Any, Future] =
    findByVideoSequenceName
      .serverLogic((videoSequenceName: String) => {
        log.atTrace.log("findByVideoSequenceName received " + videoSequenceName)
        handleErrors(mediaController.findByVideoSequenceName(videoSequenceName)
          .map(_.map(Media.from)
          .toList))
      })

  // GET v1/media/video/{name} ---------------------------------------------
  val findByVideoName: Endpoint[Unit, String, ErrorMsg, List[Media], Any] = 
    openEndpoint
      .get
      .in("v1" / "media" / "video" / path[String]("name"))
      .out(jsonBody[List[Media]])
      .name("findByVideoName")
      .description("Find media by video name")
      .tag("media")

  val findByVideoNameImpl: ServerEndpoint[Any, Future] =
    findByVideoName
      .serverLogic((videoName: String) => {
        log.atTrace.log("findByVideoName received " + videoName)
        handleErrors(mediaController.findByVideoName(videoName)
          .map(_.map(Media.from)
          .toList))
      })

  // GET v1/media/camera/{cameraId}/{startTimestamp}/{endTimestamp} ----------------
  val findByCameraIdAndTimestamps: Endpoint[Unit, (String, Instant, Instant), ErrorMsg, List[Media], Any] = 
    openEndpoint
      .get
      .in("v1" / "media" / "camera" / path[String]("cameraId") / path[Instant]("startTimestamp") / path[Instant]("endTimestamp"))
      .out(jsonBody[List[Media]])
      .name("findByCameraIdAndTimestamps")
      .description("Find media by cameraId and timestamps")
      .tag("media")

  val findByCameraIdAndTimestampsImpl: ServerEndpoint[Any, Future] =
    findByCameraIdAndTimestamps
      .serverLogic((cameraId: String, startTimestamp: Instant, endTimestamp: Instant) => {
        log.atTrace.log("findByCameraIdAndTimestamps received " + cameraId + " " + startTimestamp + " " + endTimestamp)
        handleErrors(mediaController.findByCameraIdAndTimestamps(cameraId, startTimestamp, endTimestamp)
          .map(_.map(Media.from)
          .toList))
      })

  // GET v1/media/concurrent/{videoReferenceUuid} -----------------------------------
  val findConcurrentByVideoReferenceUuid: Endpoint[Unit, UUID, ErrorMsg, List[Media], Any] = 
    openEndpoint
      .get
      .in("v1" / "media" / "concurrent" / path[UUID]("videoReferenceUuid"))
      .out(jsonBody[List[Media]])
      .name("findConcurrentByVideoReferenceUuid")
      .description("Find concurrent media by videoReferenceUuid")
      .tag("media")

  val findConcurrentByVideoReferenceUuidImpl: ServerEndpoint[Any, Future] =
    findConcurrentByVideoReferenceUuid
      .serverLogic((videoReferenceUuid: UUID) => {
        log.atTrace.log("findConcurrentByVideoReferenceUuid received " + videoReferenceUuid)
        handleErrors(mediaController.findConcurrent(videoReferenceUuid)
          .map(_.map(Media.from)
          .toList))
      })

  // GET v1/media/camera/{cameraId}/{datetime} --------------------------------------
  val findByCameraIdAndDatetime: Endpoint[Unit, (String, Instant), ErrorMsg, List[Media], Any] = 
    openEndpoint
      .get
      .in("v1" / "media" / "camera" / path[String]("cameraId") / path[Instant]("datetime"))
      .out(jsonBody[List[Media]])
      .name("findByCameraIdAndDatetime")
      .description("Find media by cameraId and datetime")
      .tag("media")

  val findByCameraIdAndDatetimeImpl: ServerEndpoint[Any, Future] =
    findByCameraIdAndDatetime
      .serverLogic((cameraId: String, datetime: Instant) => {
        log.atTrace.log("findByCameraIdAndDatetime received " + cameraId + " " + datetime)
        handleErrors(mediaController.findByCameraIdAndTimestamp(cameraId, datetime)
          .map(_.map(Media.from)
          .toList))
      })

  // GET v1/media/uri/{uri} ---------------------------------------------------------
  val findByUri: Endpoint[Unit, String, ErrorMsg, List[Media], Any] = 
    openEndpoint
      .get
      .in("v1" / "media" / "uri" / path[String]("uri"))
      .out(jsonBody[List[Media]])
      .name("findByUri")
      .description("Find media by uri")
      .tag("media")

  val findByUriImpl: ServerEndpoint[Any, Future] =
    findByUri
      .serverLogic((uri: String) => {
        log.atTrace.log("findByUri received " + uri)
        Try(URLDecoder.decode(uri, "UTF-8"))
          .map(URI.create)
          .toEither match
            case Left(_) => Future.successful(Left(BadRequest(s"Invalid URI: $uri")))
            case Right(validUri) => handleErrors(mediaController.findByURI(validUri)
              .map(_.map(Media.from)
              .toList))
      })

      

  override def all: List[Endpoint[?, ?, ?, ?, ?]] = List(createEndpoint)

  override def allImpl: List[ServerEndpoint[Any, concurrent.Future]] = List(createEndpointImpl)

}
