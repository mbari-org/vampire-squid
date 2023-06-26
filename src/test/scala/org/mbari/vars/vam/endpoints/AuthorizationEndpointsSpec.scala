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

import sttp.client3._
import sttp.client3.SttpBackend
import sttp.client3.circe.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import org.mbari.vars.vam.etc.jwt.JwtService
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sttp.model.StatusCode
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.mbari.vars.vam.etc.circe.CirceCodecs.given
import io.circe.parser.decode
import org.mbari.vars.vam.domain.BearerAuth
import org.mbari.vars.vam.etc.sdk.FutureUtil.*


class AuthorizationEndpointsSpec extends munit.FunSuite {

  given ExecutionContext = ExecutionContext.global
  val jwtService = new JwtService("mbari", "foo", "bar")
  val authorizationEndpoints = new AuthorizationEndpoints(jwtService)

  test("auth") {
    val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(authorizationEndpoints.authEndpointImpl)
      .backend()

    val response = basicRequest
      .post(uri"http://test.com/v1/auth")
      .header("APIKEY", "foo")
      .send(backendStub)

    response.map(r => {
      assertEquals(r.code, StatusCode.Ok)
      assert(r.body.isRight)

      val body = r.body.right.get
      println(body)
      val d = decode[BearerAuth](body)
      assert(d.isRight)
      val bearerAuth = d.right.get
      assert(jwtService.verify(bearerAuth.accessToken))

    }).join

  }

  
}
