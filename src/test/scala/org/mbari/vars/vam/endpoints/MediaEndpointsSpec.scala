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

import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.dao.jpa.DevelopmentTestDAOFactory
import org.mbari.vars.vam.domain.Media
import org.mbari.vars.vam.etc.jwt.JwtService
import org.mbari.vars.vam.etc.sdk.Reflect
import org.mbari.vars.vam.etc.sdk.FutureUtil.*
import scala.concurrent.{ExecutionContext, Future}
import sttp.client3.*
import sttp.client3.SttpBackend
import sttp.client3.circe.*
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import sttp.tapir.server.stub.TapirStubInterpreter


import java.net.URI
import java.time.Instant
import java.nio.charset.StandardCharsets
import org.mbari.vars.vam.etc.jdk.Logging
import org.mbari.vars.vam.etc.jdk.Logging.given
import sttp.tapir.server.interceptor.CustomiseInterceptors


class MediaEndpointsSpec extends munit.FunSuite {

  private val log = Logging(getClass)

  given ExecutionContext = ExecutionContext.global
  private val jwtService = new JwtService("mbari", "foo", "bar")
  private val daoFactory   = DevelopmentTestDAOFactory
  private val controller   = new MediaController(daoFactory)
  private val mediaEndpoints = new MediaEndpoints(controller, jwtService)

  test("POST v1/media - Create a new media") {

    val jwt = jwtService.authorize("foo").orNull

    val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(mediaEndpoints.createEndpointImpl)
      .backend()

    val now = Instant.now()
    val media = new Media(video_sequence_name = Some("Test Dive 01"),
      video_name = Some("Test Dive 01 " + now),
      camera_id = Some("Tester 01"),
      uri = Some(URI.create("http://test.me/movie01.mp4")),
      start_timestamp = Some(now))

    val request = basicRequest
      .post(uri"http://test.com/v1/media")
      .header("Authorization", s"Bearer $jwt")
      .body(Media.toFormMap(media))

    log.atDebug.log(request.toRfc2616Format(Set()))

    val response = request.send(backendStub)

    response.map(r => {
      assertEquals(r.code, StatusCode.Ok)
      assert(r.body.isRight)
    }).join

  }

  test("PUT v1/media - Update an existing media") {
    val jwt = jwtService.authorize("foo").orNull

    val now = Instant.now()
    val media0 = new Media(video_sequence_name = Some("Test Dive 02"),
      video_name = Some("Test Dive 02 " + now),
      camera_id = Some("Tester 02"),
      uri = Some(URI.create("http://test.me/movie02.mp4")),
      start_timestamp = Some(now),
      sha512 = Some(Array[Byte](1,2,3,4,5,6,7,8,9,10)))

    val media1 = controller.create(media0).join
    media1.uri = URI.create("http://test.me/movie02_changed.mp4")
    
    val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(mediaEndpoints.updateEndpointImpl)
      .backend()

    val m = Media.from(media1)
    val request = basicRequest
      .put(uri"http://test.com/v1/media")
      .header("Authorization", s"Bearer $jwt")
      .body(Media.toFormMap(m))

    log.atDebug.log(request.toRfc2616Format(Set()))

    val response = request.send(backendStub)

    response.map(r => {
      assertEquals(r.code, StatusCode.Ok)
      assert(r.body.isRight)
    }).join
  }

  
}