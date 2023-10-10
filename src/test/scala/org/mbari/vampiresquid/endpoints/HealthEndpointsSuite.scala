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

import scala.concurrent.ExecutionContext
import org.mbari.vampiresquid.etc.sdk.FutureUtil.join
import sttp.client3.*
import sttp.client3.circe.*
import sttp.client3.SttpBackend
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import scala.concurrent.Future
import sttp.tapir.server.stub.TapirStubInterpreter

class HealthEndpointsSuite extends munit.FunSuite:
  given ExecutionContext = ExecutionContext.global
  val healthEndpoints = new HealthEndpoints

  test("health"):
    val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(healthEndpoints.healthEndpointImpl)
      .backend()

    val request = basicRequest.get(uri"http://test.com/v1/health")
    val response = request.send(backendStub)
    response.map(r => {
      assertEquals(r.code, StatusCode.Ok)
    }).join
    // val body = response.body
    // assert(body.isRight)
    // println(body)


