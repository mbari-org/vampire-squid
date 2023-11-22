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
import org.mbari.vampiresquid.domain.BadRequest
import sttp.tapir.EndpointIO.annotations.description

class VideoReferenceEndpoints(controller: VideoReferenceController)(using ec: ExecutionContext, jwtService: JwtService)
    extends Endpoints:

    private val hex = HexFormat.of()

    // GET "v1/videoreferences"
    val findAllEndpoint: Endpoint[Unit, Unit, ErrorMsg, List[VideoReference], Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences")
            .out(jsonBody[List[VideoReference]])
            .name("findAll")
            .description("Find all video references")
            .tag("video references")

    val findAllEndpointImpl: ServerEndpoint[Any, Future] =
        findAllEndpoint.serverLogic { _ =>
            handleErrors(controller.findAll().map(_.toList))
        }

    // GET "v1/videoreferences/:uuid"
    val findOneEndpoint: Endpoint[Unit, UUID, ErrorMsg, VideoReference, Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / path[UUID]("uuid"))
            .out(jsonBody[VideoReference])
            .name("findOne")
            .description("Find a video reference by UUID")
            .tag("video references")

    val findOneEndpointImpl: ServerEndpoint[Any, Future] =
        findOneEndpoint.serverLogic { uuid =>
            handleOption(controller.findByUUID(uuid))
        }

    // GET "v1/videoreferences/lastupdate/:uuid"
    val findLastUpdateEndpoint: Endpoint[Unit, UUID, ErrorMsg, LastUpdatedTime, Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / "lastupdate" / path[UUID]("uuid"))
            .out(jsonBody[LastUpdatedTime])
            .name("findLastUpdate")
            .description("Find the last update time for a video reference by UUID")
            .tag("video references")

    val findLastUpdateEndpointImpl: ServerEndpoint[Any, Future] =
        findLastUpdateEndpoint.serverLogic { uuid =>
            handleOption(
                controller.findByUUID(uuid).map(opt => opt.flatMap(v => v.last_updated_time.map(LastUpdatedTime(_))))
            )
        }

    // GET "v1/videoreferences/uri/:uri"
    val findByUriEndpoint: Endpoint[Unit, URI, ErrorMsg, VideoReference, Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / "uri" / path[URI]("uri"))
            .out(jsonBody[VideoReference])
            .name("findByUri")
            .description("Find a video reference by URI")
            .tag("video references")

    val findByUriEndpointImpl: ServerEndpoint[Any, Future] =
        findByUriEndpoint.serverLogic { uri =>
            handleOption(controller.findByURI(uri))
        }

    // GET "v1/videoreferences/uris"
    val findAllUrisEndpoint: Endpoint[Unit, Unit, ErrorMsg, List[URI], Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / "uris")
            .out(jsonBody[List[URI]])
            .name("findByUris")
            .description("Find video references by URIs")
            .tag("video references")

    val findAllUrisEndpointImpl: ServerEndpoint[Any, Future] =
        findAllUrisEndpoint.serverLogic { _ =>
            handleErrors(controller.findAllURIs().map(_.toList))
        }

    // GET v1/videoreferences/sha512/:sha512
    val findBySha512Endpoint: Endpoint[Unit, String, ErrorMsg, VideoReference, Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / "sha512" / path[String]("sha512"))
            .out(jsonBody[VideoReference])
            .name("findBySha512")
            .description("Find a video reference by SHA512")
            .tag("video references")
    val findBySha512EndpointImpl: ServerEndpoint[Any, Future]                       =
        findBySha512Endpoint.serverLogic { sha512 =>
            handleOption(controller.findBySha512(hex.parseHex(sha512)))
        }

    // DELETE "v1/videoreferences/:uuid"
    val deleteEndpoint: Endpoint[Option[String], UUID, ErrorMsg, Unit, Any] =
        secureEndpoint
            .delete
            .in("v1" / "videoreferences" / path[UUID]("uuid"))
            .out(statusCode(StatusCode.NoContent))
            .name("delete")
            .description("Delete a video reference by UUID")
            .tag("video references")

    val deleteEndpointImpl: ServerEndpoint[Any, Future] =
        deleteEndpoint
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic { _ => uuid =>
                handleErrors(controller.delete(uuid).map(b => if b then () else throw new Exception("Not found")))
            }

    // POST "v1/videoreferences" (form body)
    val createEndpoint: Endpoint[Option[String], Map[String, String], ErrorMsg, VideoReference, Any] =
        secureEndpoint
            .post
            .in("v1" / "videoreferences")
            .in(formBody[Map[String, String]])
            .out(jsonBody[VideoReference])
            .name("create")
            .description("Create a video reference")
            .tag("video references")

    val createEndpointImpl: ServerEndpoint[Any, Future] =
        createEndpoint
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic { _ => form =>
                val videoUuid = form.get("description").map(UUID.fromString)
                val uri       = form.get("uri").map(URI.create)
                if videoUuid.isEmpty || uri.isEmpty then
                    Future(Left(BadRequest("Missing required parameters: video_uuid, uri ")))
                else
                    val description = form.get("description")
                    val container   = form.get("container")
                    val videoCodec  = form.get("video_codec")
                    val audioCodec  = form.get("audio_codec")
                    val width       = form.get("width").map(_.toInt)
                    val height      = form.get("height").map(_.toInt)
                    val frameRate   = form.get("frame_rate").map(_.toDouble)
                    val sizeBytes   = form.get("size_bytes").map(_.toLong)
                    val sha512      = form.get("sha512").map(hex.parseHex)
                    handleErrors(
                        controller.create(
                            videoUuid.get,
                            uri.get,
                            container,
                            videoCodec,
                            audioCodec,
                            width,
                            height,
                            frameRate,
                            sizeBytes,
                            description,
                            sha512
                        )
                    )
            }

    // PUT "v1/videoreferences/:uuid" (form body)
    val updateEndpoint: Endpoint[Option[String], (UUID, Map[String, String]), ErrorMsg, VideoReference, Any] =
        secureEndpoint
            .put
            .in("v1" / "videoreferences" / path[UUID]("uuid"))
            .in(formBody[Map[String, String]])
            .out(jsonBody[VideoReference])
            .name("update")
            .description("Update a video reference by UUID")
            .tag("video references")

    val updateEndpointImpl: ServerEndpoint[Any, Future] =
        updateEndpoint
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic { _ => (uuid, form) =>
                val videoUuid   = form.get("description").map(UUID.fromString)
                val uri         = form.get("uri").map(URI.create)
                val description = form.get("description")
                val container   = form.get("container")
                val videoCodec  = form.get("video_codec")
                val audioCodec  = form.get("audio_codec")
                val width       = form.get("width").map(_.toInt)
                val height      = form.get("height").map(_.toInt)
                val frameRate   = form.get("frame_rate").map(_.toDouble)
                val sizeBytes   = form.get("size_bytes").map(_.toLong)
                val sha512      = form.get("sha512").map(hex.parseHex)
                handleErrors(
                    controller.update(
                        uuid,
                        videoUuid,
                        uri,
                        container,
                        videoCodec,
                        audioCodec,
                        width,
                        height,
                        frameRate,
                        sizeBytes,
                        description,
                        sha512
                    )
                )
            }

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

    override def allImpl: List[ServerEndpoint[Any, Future]] = List(
        findAllEndpointImpl,
        findOneEndpointImpl,
        findLastUpdateEndpointImpl,
        findByUriEndpointImpl,
        findAllUrisEndpointImpl,
        findBySha512EndpointImpl,
        deleteEndpointImpl,
        createEndpointImpl,
        updateEndpointImpl
    )
