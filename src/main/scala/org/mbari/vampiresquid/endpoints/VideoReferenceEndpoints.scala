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

import org.mbari.vampiresquid.controllers.VideoReferenceController
import org.mbari.vampiresquid.etc.jwt.JwtService
import scala.concurrent.ExecutionContext
import org.mbari.vampiresquid.domain.VideoReference
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import org.mbari.vampiresquid.etc.jdk.Logging
import org.mbari.vampiresquid.etc.jdk.Logging.given
import java.net.URI
import java.util.HexFormat
import org.mbari.vampiresquid.domain.ErrorMsg
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import java.util.UUID
import org.mbari.vampiresquid.domain.LastUpdatedTime
import org.mbari.vampiresquid.etc.tapir.TapirCodecs.given
import sttp.model.StatusCode
import scala.concurrent.Future

class VideoReferenceEndpoints(controller: VideoReferenceController, jwtService: JwtService)(using ec: ExecutionContext) extends Endpoints:

  given Schema[URI]                 = Schema.string
  given Schema[Option[URI]]         = Schema.string
  given Schema[VideoReference]      = Schema.derived[VideoReference]
  given givenJwtService: JwtService = jwtService
  private val log                   = Logging(getClass)
  private val hex                   = HexFormat.of()

  // GET "v1/videoreferences"
  val findAllEndpoint: Endpoint[Unit, Unit, ErrorMsg, List[VideoReference], Any] =
    openEndpoint
      .get
      .in("v1" / "videoreferences")
      .out(jsonBody[List[VideoReference]])
      .name("findAll")
      .description("Find all video references")
      .tag("video references")

  // GET "v1/videoreferences/:uuid"
  val findOneEndpoint: Endpoint[Unit, UUID, ErrorMsg, VideoReference, Any] =
    openEndpoint
      .get
      .in("v1" / "videoreferences" / path[UUID]("uuid"))
      .out(jsonBody[VideoReference])
      .name("findOne")
      .description("Find a video reference by UUID")
      .tag("video references")

  // GET "v1/videoreferences/lastupdate/:uuid"
  val findLastUpdateEndpoint: Endpoint[Unit, UUID, ErrorMsg, LastUpdatedTime, Any] =
    openEndpoint
      .get
      .in("v1" / "videoreferences" / "lastupdate" / path[UUID]("uuid"))
      .out(jsonBody[LastUpdatedTime])
      .name("findLastUpdate")
      .description("Find the last update time for a video reference by UUID")
      .tag("video references")

  // GET "v1/videoreferences/uri/:uri"
  val findByUriEndpoint: Endpoint[Unit, URI, ErrorMsg, VideoReference, Any] =
    openEndpoint
      .get
      .in("v1" / "videoreferences" / "uri" / path[URI]("uri"))
      .out(jsonBody[VideoReference])
      .name("findByUri")
      .description("Find a video reference by URI")
      .tag("video references")

  // GET "v1/videoreferences/uris"
  val findAllUrisEndpoint: Endpoint[Unit, Unit, ErrorMsg, List[URI], Any] =
    openEndpoint
      .get
      .in("v1" / "videoreferences" / "uris")
      .out(jsonBody[List[URI]])
      .name("findByUris")
      .description("Find video references by URIs")
      .tag("video references")

  // GET v1/videoreferences/sha512/:sha512
  val findBySha512Endpoint: Endpoint[Unit, String, ErrorMsg, VideoReference, Any] =
    openEndpoint
      .get
      .in("v1" / "videoreferences" / "sha512" / path[String]("sha512"))
      .out(jsonBody[VideoReference])
      .name("findBySha512")
      .description("Find a video reference by SHA512")
      .tag("video references")

  // DELETE "v1/videoreferences/:uuid"
  val deleteEndpoint: Endpoint[Option[String], UUID, ErrorMsg, Unit, Any] =
    secureEndpoint
      .delete
      .in("v1" / "videoreferences" / path[UUID]("uuid"))
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .out(statusCode(StatusCode.NoContent))
      .name("delete")
      .description("Delete a video reference by UUID")
      .tag("video references")

  // POST "v1/videoreferences" (form body)
  val createEndpoint: Endpoint[Option[String], Map[String, String], ErrorMsg, VideoReference, Any] =
    secureEndpoint
      .post
      .in("v1" / "videoreferences")
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .in(formBody[Map[String, String]])
      .out(jsonBody[VideoReference])
      .name("create")
      .description("Create a video reference")
      .tag("video references")

  // PUT "v1/videoreferences/:uuid" (form body)
  val updateEndpoint: Endpoint[Option[String], (UUID, Map[String, String]), ErrorMsg, VideoReference, Any] =
    secureEndpoint
      .put
      .in("v1" / "videoreferences" / path[UUID]("uuid"))
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .in(formBody[Map[String, String]])
      .out(jsonBody[VideoReference])
      .name("update")
      .description("Update a video reference by UUID")
      .tag("video references")

  override val all: List[Endpoint[_, _, _, _, _]] = List(
    findAllEndpoint,
    findOneEndpoint,
    findLastUpdateEndpoint,
    findByUriEndpoint,
    findAllUrisEndpoint,
    findBySha512Endpoint,
    deleteEndpoint,
    createEndpoint,
    updateEndpoint
  )

  override def allImpl: List[ServerEndpoint[Any, Future]] = ???
