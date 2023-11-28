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
import java.util.HexFormat
import java.util.UUID
import org.mbari.vampiresquid.controllers.VideoReferenceController
import org.mbari.vampiresquid.domain.BadRequest
import org.mbari.vampiresquid.domain.ErrorMsg
import org.mbari.vampiresquid.domain.LastUpdatedTime
import org.mbari.vampiresquid.domain.VideoReference
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import org.mbari.vampiresquid.etc.jdk.Logging
import org.mbari.vampiresquid.etc.jdk.Logging.given
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.tapir.TapirCodecs.given
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import scala.util.chaining.*
import org.mbari.vampiresquid.domain.VideoReferenceCreate
import org.mbari.vampiresquid.domain.VideoReferenceUpdate

class VideoReferenceEndpoints(controller: VideoReferenceController)(using ec: ExecutionContext, jwtService: JwtService)
    extends Endpoints:

    private val hex = HexFormat.of()

    // GET "v1/videoreferences"
    val findAllVideoReferences: Endpoint[Unit, Unit, ErrorMsg, List[VideoReference], Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences")
            .out(jsonBody[List[VideoReference]])
            .name("findAll")
            .description("Find all video references")
            .tag("video references")

    val findAllVideoReferencesImpl: ServerEndpoint[Any, Future] =
        findAllVideoReferences.serverLogic { _ =>
            handleErrors(controller.findAll().map(_.toList))
        }

    // GET "v1/videoreferences/:uuid"
    val findOneVideoReference: Endpoint[Unit, UUID, ErrorMsg, VideoReference, Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / path[UUID]("uuid"))
            .out(jsonBody[VideoReference])
            .name("findOne")
            .description("Find a video reference by UUID")
            .tag("video references")

    val findOneVideoReferenceImpl: ServerEndpoint[Any, Future] =
        findOneVideoReference.serverLogic { uuid =>
            handleOption(controller.findByUUID(uuid))
        }

    // GET "v1/videoreferences/lastupdate/:uuid"
    val findLastUpdateForVideoReference: Endpoint[Unit, UUID, ErrorMsg, LastUpdatedTime, Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / "lastupdate" / path[UUID]("uuid"))
            .out(jsonBody[LastUpdatedTime])
            .name("findLastUpdate")
            .description("Find the last update time for a video reference by UUID")
            .tag("video references")

    val findLastUpdateForVideoReferenceImpl: ServerEndpoint[Any, Future] =
        findLastUpdateForVideoReference.serverLogic { uuid =>
            handleOption(
                controller.findByUUID(uuid).map(opt => opt.flatMap(v => v.last_updated_time.map(LastUpdatedTime(_))))
            )
        }

    // GET "v1/videoreferences/uri/:uri"
    val findVideoReferenceByUri: Endpoint[Unit, URI, ErrorMsg, VideoReference, Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / "uri" / path[URI]("uri"))
            .out(jsonBody[VideoReference])
            .name("findByUri")
            .description("Find a video reference by URI")
            .tag("video references")

    val findVideoReferenceByUriImpl: ServerEndpoint[Any, Future] =
        findVideoReferenceByUri.serverLogic { uri =>
            handleOption(controller.findByURI(uri))
        }

    // GET "v1/videoreferences/uris"
    val findAllUris: Endpoint[Unit, Unit, ErrorMsg, List[URI], Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / "uris")
            .out(jsonBody[List[URI]])
            .name("findByUris")
            .description("Find video references by URIs")
            .tag("video references")

    val findAllUrisImpl: ServerEndpoint[Any, Future] =
        findAllUris.serverLogic { _ =>
            handleErrors(controller.findAllURIs().map(_.toList))
        }

    // GET v1/videoreferences/sha512/:sha512
    val findVideoReferenceBySha512: Endpoint[Unit, String, ErrorMsg, VideoReference, Any] =
        openEndpoint
            .get
            .in("v1" / "videoreferences" / "sha512" / path[String]("sha512"))
            .out(jsonBody[VideoReference])
            .name("findBySha512")
            .description("Find a video reference by SHA512")
            .tag("video references")
    val findVideoReferenceBySha512Impl: ServerEndpoint[Any, Future]                       =
        findVideoReferenceBySha512.serverLogic { sha512 =>
            handleOption(controller.findBySha512(hex.parseHex(sha512)))
        }

    // DELETE "v1/videoreferences/:uuid"
    val deleteOneVideoReference: Endpoint[Option[String], UUID, ErrorMsg, Unit, Any] =
        secureEndpoint
            .delete
            .in("v1" / "videoreferences" / path[UUID]("uuid"))
            .out(statusCode(StatusCode.NoContent))
            .name("delete")
            .description("Delete a video reference by UUID")
            .tag("video references")

    val deleteOneVideoReferenceImpl: ServerEndpoint[Any, Future] =
        deleteOneVideoReference
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic { _ => uuid =>
                handleErrors(controller.delete(uuid).map(b => if b then () else throw new Exception("Not found")))
            }

    // POST "v1/videoreferences" (form body)
    val createOneVideoReference: Endpoint[Option[String], VideoReferenceCreate, ErrorMsg, VideoReference, Any] =
        secureEndpoint
            .post
            .in("v1" / "videoreferences")
            .in(oneOfBody(formBody[VideoReferenceCreate], jsonBody[VideoReferenceCreate]))
            .out(jsonBody[VideoReference])
            .name("create")
            .description("Create a video reference")
            .tag("video references")

    val createOneVideoReferenceImpl: ServerEndpoint[Any, Future] =
        createOneVideoReference
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic { _ => req =>
                handleErrors(
                    controller.create(
                        req.video_uuid,
                        req.uri,
                        req.container,
                        req.video_codec,
                        req.audio_codec,
                        req.width,
                        req.height,
                        req.frame_rate,
                        req.size_bytes,
                        req.description,
                        req.sha512
                    )
                )
            }

    // PUT "v1/videoreferences/:uuid" (form body)
    val updateOneVideoReference: Endpoint[Option[String], (UUID, VideoReferenceUpdate), ErrorMsg, VideoReference, Any] =
        secureEndpoint
            .put
            .in("v1" / "videoreferences" / path[UUID]("uuid"))
            .in(oneOfBody(formBody[VideoReferenceUpdate], jsonBody[VideoReferenceUpdate]))
            .out(jsonBody[VideoReference])
            .name("update")
            .description("Update a video reference by UUID")
            .tag("video references")

    val updateOneVideoReferenceImpl: ServerEndpoint[Any, Future] =
        updateOneVideoReference
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic { _ => (uuid, req) =>
                handleErrors(
                    controller.update(
                        uuid,
                        req.video_uuid,
                        req.uri,
                        req.container,
                        req.video_codec,
                        req.audio_codec,
                        req.width,
                        req.height,
                        req.frame_rate,
                        req.size_bytes,
                        req.description,
                        req.sha512
                    )
                )
            }

    override val all: List[Endpoint[_, _, _, _, _]] = List(
        findAllVideoReferences,
        findOneVideoReference,
        findLastUpdateForVideoReference,
        findVideoReferenceByUri,
        findAllUris,
        findVideoReferenceBySha512,
        deleteOneVideoReference,
        createOneVideoReference,
        updateOneVideoReference
    )

    override def allImpl: List[ServerEndpoint[Any, Future]] = List(
        findAllVideoReferencesImpl,
        findOneVideoReferenceImpl,
        findLastUpdateForVideoReferenceImpl,
        findVideoReferenceByUriImpl,
        findAllUrisImpl,
        findVideoReferenceBySha512Impl,
        deleteOneVideoReferenceImpl,
        createOneVideoReferenceImpl,
        updateOneVideoReferenceImpl
    )
