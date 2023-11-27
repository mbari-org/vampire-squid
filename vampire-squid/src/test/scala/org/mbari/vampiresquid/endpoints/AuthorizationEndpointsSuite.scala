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

import sttp.client3.*
import sttp.client3.SttpBackend
import sttp.client3.circe.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import org.mbari.vampiresquid.etc.jwt.JwtService
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import sttp.model.StatusCode
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import io.circe.parser.decode
import org.mbari.vampiresquid.domain.Authorization
import org.mbari.vampiresquid.etc.sdk.FutureUtil.*
import sttp.tapir.server.interceptor.exception.ExceptionHandler
import sttp.tapir.server.interceptor.CustomiseInterceptors
import sttp.tapir.server.vertx.VertxFutureServerOptions
import sttp.tapir.server.model.ValuedEndpointOutput

class AuthorizationEndpointsSuite extends munit.FunSuite:

    given ExecutionContext     = ExecutionContext.global
    val jwtService             = new JwtService("mbari", "foo", "bar")
    val authorizationEndpoints = new AuthorizationEndpoints(jwtService)

    test("auth"):

        // --- START: This block adds exception logging to the stub
        val exceptionHandler = ExceptionHandler.pure[Future](ctx =>
            Some(
                ValuedEndpointOutput(
                    sttp.tapir.stringBody.and(sttp.tapir.statusCode),
                    (s"failed due to ${ctx.e.getMessage}", StatusCode.InternalServerError)
                )
            )
        )

        val customOptions: CustomiseInterceptors[Future, VertxFutureServerOptions] =
            import scala.concurrent.ExecutionContext.Implicits.global
            VertxFutureServerOptions
                .customiseInterceptors
                .exceptionHandler(exceptionHandler)
        // --- END: This block adds exception logging to the stub

        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(customOptions, SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(authorizationEndpoints.authEndpointImpl)
            .backend()

        val response = basicRequest
            .post(uri"http://test.com/v1/auth")
            .header("APIKEY", "foo")
            .send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)

                val body       = r.body.right.get
                // println(body)
                val d          = decode[Authorization](body)
                assert(d.isRight)
                val bearerAuth = d.right.get
                assert(jwtService.verify(bearerAuth.accessToken))
            )
            .join
