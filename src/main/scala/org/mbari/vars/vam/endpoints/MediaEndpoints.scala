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
import org.mbari.vars.vam.auth.AuthorizationService
import org.mbari.vars.vam.domain.{ErrorMsg, Unauthorized}
import org.mbari.vars.vam.domain.Media
import org.mbari.vars.vam.etc.circe.CirceCodecs.given
import scala.concurrent.ExecutionContext
import sttp.model.headers.WWWAuthenticateChallenge
import org.mbari.vars.vam.auth.Authorization
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import java.net.URI
import scala.concurrent.Future
import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.etc.jwt.JwtService

class MediaEndpoints(mediaController: MediaController, jwtService: JwtService)(using ec: ExecutionContext) extends Endpoints {
  
  given Schema[Option[URI]] = Schema.string
  given givenJwtService: JwtService = jwtService

  val createEndpoint: Endpoint[Option[String], Media, ErrorMsg, Media, Any] = 
    secureEndpoint
      .post
      .in("v1" / "media")
      .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
      .in(multipartBody[Media])
      .out(jsonBody[Media])
      .name("create")
      .description("Create a new media")
      .tag("media")

  val createEndpointImpl: ServerEndpoint[Any, Future] =
    createEndpoint
      .serverSecurityLogic(jwtOpt => verify(jwtOpt))
      .serverLogic(_ => media => mediaController.create(media)
          .map(Media.from(_))
          .map(Right(_)))

  override def all: List[Endpoint[?, ?, ?, ?, ?]] = ???

  override def allImpl: List[ServerEndpoint[Any, concurrent.Future]] = ???

}
