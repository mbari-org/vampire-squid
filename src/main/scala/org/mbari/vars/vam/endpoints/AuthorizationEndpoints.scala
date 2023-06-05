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



import scala.concurrent.ExecutionContext
import sttp.tapir.Endpoint
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import org.mbari.vars.vam.domain.{ErrorMsg, BearerAuth, Unauthorized}
import scala.concurrent.Future
import org.mbari.vars.vam.etc.jdk.Logging.given
import org.mbari.vars.vam.etc.jwt.JwtService
import org.mbari.vars.vam.etc.circe.CirceCodecs.given
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.EndpointIO.annotations.apikey 

class AuthorizationEndpoints(jwtService: JwtService)(using ec: ExecutionContext) extends Endpoints {

  private val log = System.getLogger(getClass().getName())

  val authEndpoint: Endpoint[String, Unit, ErrorMsg, BearerAuth, Any] =
      secureEndpoint
        .post
        .in("v1" / "auth")
        .securityIn(header[String]("APIKEY"))
        .out(jsonBody[BearerAuth])
        .name("authenticate")
        .description("Exchange an API key for a JWT")
        .tag("auth")

  val authEndpointImpl: ServerEndpoint[Any, Future] =
    authEndpoint
      .serverSecurityLogic(apiKey => jwtService.authorize(apiKey) match
              case None => Future(Left(Unauthorized("Invalid API key")))
              case Some(jwt) => Future(Right(BearerAuth(jwt))))
      .serverLogic(bearerAuth => Unit => Future(Right(bearerAuth)))
      
  override val all: List[Endpoint[?, ?, ?, ?, ?]]         = List(authEndpoint)
  override val allImpl: List[ServerEndpoint[Any, Future]] =
    List(authEndpointImpl)
  
}
