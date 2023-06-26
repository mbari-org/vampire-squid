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

import io.circe.generic.auto.*

import java.net.URI
import java.nio.charset.StandardCharsets
import org.glassfish.jaxb.core.api.impl.NameConverter.Standard
import org.mbari.vars.vam.auth.Authorization
import org.mbari.vars.vam.auth.AuthorizationService
import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.domain.{ErrorMsg, Media, NotFound, ServerError, Unauthorized}
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


class MediaEndpoints(mediaController: MediaController, jwtService: JwtService)(using ec: ExecutionContext) extends Endpoints {
  
  given Schema[Option[URI]] = Schema.string
  given Schema[Media] = Schema.derived[Media]
  given givenJwtService: JwtService = jwtService
  private val log = Logging(getClass)

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
        mediaController.create(media)
          .map(m => Media.from(m))
          .map(Right(_))
          .recover({
            case NonFatal(e) =>
              log.atDebug.withCause(e).log("createEndpointImpl failed")
              Left(ServerError("Update for media failed: " + e.getMessage))
          })
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
        mediaController.updateMedia(mutableMedia)
          .map {
            case None => Left(NotFound("Not found"))
            case Some(newMutableMedia) => Right(Media.from(newMutableMedia))
          }
          .recover({
            case NonFatal(e) =>
              log.atDebug.withCause(e).log("createEndpointImpl failed")
              Left(ServerError("Update for media failed: " + e.getMessage))
          })
      })
      

  override def all: List[Endpoint[?, ?, ?, ?, ?]] = List(createEndpoint)

  override def allImpl: List[ServerEndpoint[Any, concurrent.Future]] = List(createEndpointImpl)

}
