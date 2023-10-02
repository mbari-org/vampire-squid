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

package org.mbari

import sttp.tapir.*

import Library.*
import io.circe.generic.auto.*
import scala.concurrent.Future
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.controllers.MediaController
import org.mbari.vampiresquid.AppConfig
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.endpoints.MediaEndpoints
import org.mbari.vampiresquid.endpoints.AuthorizationEndpoints
import org.mbari.vampiresquid.endpoints.HealthEndpoints
import scala.concurrent.ExecutionContext.Implicits.global

object Endpoints:
  case class User(name: String) extends AnyVal
  val helloEndpoint: PublicEndpoint[User, Unit, String, Any] = endpoint
    .get
    .in("hello")
    .in(query[User]("name"))
    .out(stringBody)
  val helloServerEndpoint: ServerEndpoint[Any, Future]       = helloEndpoint.serverLogicSuccess(user => Future.successful(s"Hello ${user.name}"))

  val booksListing: PublicEndpoint[Unit, Unit, List[Book], Any] = endpoint
    .get
    .in("books" / "list" / "all")
    .out(jsonBody[List[Book]])
  val booksListingServerEndpoint: ServerEndpoint[Any, Future]   = booksListing.serverLogicSuccess(_ => Future.successful(Library.books))

  // ----------------------------
  val daoFactory      = JPADAOFactory
  val mediaController = new MediaController(daoFactory)

  val jwtParams       = AppConfig.JwtParameters
  val jwtService      = new JwtService(jwtParams.issuer, jwtParams.clientSecret, jwtParams.signingSecret)
  val mediaEndpoints  = new MediaEndpoints(mediaController, jwtService)
  val authEndpoints   = new AuthorizationEndpoints(jwtService)
  val healthEndpoints = new HealthEndpoints

  val apiEndpoints = mediaEndpoints.allImpl ++ authEndpoints.allImpl ++ healthEndpoints.allImpl

  // val apiEndpoints: List[ServerEndpoint[Any, Future]] = List(helloServerEndpoint, booksListingServerEndpoint)

  val docEndpoints: List[ServerEndpoint[Any, Future]] = SwaggerInterpreter()
    .fromServerEndpoints[Future](apiEndpoints, "vampire-squid", "1.0.0")

  val prometheusMetrics: PrometheusMetrics[Future] = PrometheusMetrics.default[Future]()
  val metricsEndpoint: ServerEndpoint[Any, Future] = prometheusMetrics.metricsEndpoint

  val all: List[ServerEndpoint[Any, Future]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)

object Library:
  case class Author(name: String)
  case class Book(title: String, year: Int, author: Author)

  val books = List(
    Book("The Sorrows of Young Werther", 1774, Author("Johann Wolfgang von Goethe")),
    Book("On the Niemen", 1888, Author("Eliza Orzeszkowa")),
    Book("The Art of Computer Programming", 1968, Author("Donald Knuth")),
    Book("Pharaoh", 1897, Author("Boleslaw Prus"))
  )
