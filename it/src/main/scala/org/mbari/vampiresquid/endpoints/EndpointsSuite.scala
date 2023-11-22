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

import io.circe.*
import io.circe.parser.*
import org.mbari.vampiresquid.etc.sdk.FutureUtil.*
import org.mbari.vampiresquid.repository.jpa.BaseDAOSuite
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter

import scala.concurrent.Future

trait EndpointsSuite extends BaseDAOSuite:

    def runGet(
        ep: ServerEndpoint[Any, Future],
        uri: String,
        assertions: Response[Either[String, String]] => Unit
    ): Unit =
        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(ep)
            .backend()
        val u                                     = uri"$uri"
//    println(u)
        val request                               = basicRequest.get(u)
        val response                              = request.send(backendStub).join
        assertions(response)

    def checkResponse[T: Decoder](responseBody: Either[String, String]): T =
        responseBody match
            case Left(e)     => fail(e)
            case Right(json) =>
                decode[T](json) match
                    case Left(error)  => fail(error.getLocalizedMessage)
                    case Right(value) => value
