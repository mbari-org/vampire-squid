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

import org.mbari.vampiresquid.controllers.VideoController
import org.mbari.vampiresquid.etc.jwt.JwtService
import scala.concurrent.ExecutionContext
import sttp.tapir.Endpoint
import scala.concurrent.Future
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import org.mbari.vampiresquid.domain.Video
import org.mbari.vampiresquid.domain.VideoReference
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import org.mbari.vampiresquid.domain.ErrorMsg
import java.net.URI
import java.util.UUID
import org.mbari.vampiresquid.domain.LastUpdatedTime
import java.time.Instant
import sttp.model.headers.WWWAuthenticateChallenge

class VideoEndpoints(controller: VideoController, jwtService: JwtService)(using ec: ExecutionContext) extends Endpoints:

  given Schema[URI]            = Schema.string
  given Schema[VideoReference] = Schema.derived[VideoReference]

  // GET v1/videos
  val findAllEndpoint: Endpoint[Unit, Unit, ErrorMsg, List[Video], Any] =
    openEndpoint
      .get
      .in("v1" / "videos")
      .out(jsonBody[List[Video]])
      .name("findAll")
      .description("Find all videos")
      .tag("videos")

  // GET v1/videos/:uuid
  val findOneEndpoint: Endpoint[Unit, UUID, ErrorMsg, Video, Any] =
    openEndpoint
      .get
      .in("v1" / "videos" / path[UUID]("uuid"))
      .out(jsonBody[Video])
      .name("findOne")
      .description("Find a video by UUID")
      .tag("videos")

  // GET v1/videos/videosequence/:uuid
  val findVideoByVideoSequenceUuid: Endpoint[Unit, UUID, ErrorMsg, List[Video], Any] =
    openEndpoint
      .get
      .in("v1" / "videos" / "videosequence" / path[UUID]("videoSequenceUuid"))
      .out(jsonBody[List[Video]])
      .name("findVideoByVideoSequenceUuid")
      .description("Find a videos by its video sequence UUID")
      .tag("videos")

  // GET v1/videos/videoreference/:uuid/
  val findVideoByVideoReferenceUuid: Endpoint[Unit, UUID, ErrorMsg, List[Video], Any] =
    openEndpoint
      .get
      .in("v1" / "videos" / "videoreference" / path[UUID]("videoReferenceUuid"))
      .out(jsonBody[List[Video]])
      .name("findVideoByVideoReferenceUuid")
      .description("Find a videos by its video reference UUID")
      .tag("videos")

  // get v1/videos/lastupdate/:uuid
  val findLastUpdateEndpoint: Endpoint[Unit, UUID, ErrorMsg, LastUpdatedTime, Any] =
    openEndpoint
      .get
      .in("v1" / "videos" / "lastupdate" / path[UUID]("uuid"))
      .out(jsonBody[LastUpdatedTime])
      .name("findLastUpdate")
      .description("Find the last update time for a video by UUID")
      .tag("videos")

  // GET v1/videos/name/:name
  val findVideoByName: Endpoint[Unit, String, ErrorMsg, List[Video], Any] =
    openEndpoint
      .get
      .in("v1" / "videos" / "name" / path[String]("name"))
      .out(jsonBody[List[Video]])
      .name("findVideoByName")
      .description("Find a videos by its name")
      .tag("videos")

  // GET v1/videosequence/:name
  val findVideoByVideoSequenceByName: Endpoint[Unit, String, ErrorMsg, List[Video], Any] =
    openEndpoint
      .get
      .in("v1" / "videos" / "videosequence" / "name" / path[String]("videoSequenceName"))
      .out(jsonBody[List[Video]])
      .name("findVideoByVideoSequenceByName")
      .description("Find videos by its video sequence name")
      .tag("videos")

  // GET v1/videos/timestamp/:timestamp
  val findVideoByTimestamp: Endpoint[Unit, Instant, ErrorMsg, List[Video], Any] =
    openEndpoint
      .get
      .in("v1" / "videos" / "timestamp" / path[Instant]("timestamp"))
      .out(jsonBody[List[Video]])
      .name("findVideoByTimestamp")
      .description("Find videos by its timestamp")
      .tag("videos")

  // GET v1/videos/timestamp/:start/:end
  val findVideoByTimestampRange: Endpoint[Unit, (Instant, Instant), ErrorMsg, List[Video], Any] =
    openEndpoint
      .get
      .in("v1" / "videos" / "timestamp" / path[Instant]("start") / path[Instant]("end"))
      .out(jsonBody[List[Video]])
      .name("findVideoByTimestampRange")
      .description("Find videos by its timestamp range")
      .tag("videos")

  // DELETE v1/videos/:uuid
  val deleteOneEndpoint: Endpoint[Option[String], UUID, ErrorMsg, Unit, Any] =
    secureEndpoint
      .delete
      .in("v1" / "videos" / path[UUID]("uuid"))
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .out(jsonBody[Unit])
      .name("deleteOne")
      .description("Delete a video by UUID")
      .tag("videos")

  // POST v1/videos (form body)
  val createOneEndpoint: Endpoint[Option[String], Map[String, String], ErrorMsg, Video, Any] =
    secureEndpoint
      .post
      .in("v1" / "videos")
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .in(formBody[Map[String, String]])
      .out(jsonBody[Video])
      .name("createOne")
      .description("Create a video")
      .tag("videos")

  // PUT v1/videos/:uuid (form body)
  val updateEndpoint: Endpoint[Option[String], (UUID, Map[String, String]), ErrorMsg, Video, Any] =
    secureEndpoint
      .put
      .in("v1" / "videos" / path[UUID]("uuid"))
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .in(formBody[Map[String, String]])
      .out(jsonBody[Video])
      .name("update")
      .description("Update a video by UUID")
      .tag("videos")

  override def all: List[Endpoint[?, ?, ?, ?, ?]] = ???

  override def allImpl: List[ServerEndpoint[Any, Future]] = ???
