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

package org.mbari.vampiresquid

import org.mbari.vampiresquid.Endpoints.{*, given}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter

import Library.*
import io.circe.generic.auto.*
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import sttp.client3.circe.*


class EndpointsSpec extends AnyFlatSpec with Matchers with EitherValues:

  it should "return hello message" in:
    // given
    val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(helloServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/hello?name=adam")
      .send(backendStub)

    // then
    response.map(_.body.value shouldBe "Hello adam").unwrap

  it should "list available books" in:
    // given
    val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
      .whenServerEndpointRunLogic(booksListingServerEndpoint)
      .backend()

    // when
    val response = basicRequest
      .get(uri"http://test.com/books/list/all")
      .response(asJson[List[Book]])
      .send(backendStub)

    // then
    response.map(_.body.value shouldBe books).unwrap

  extension [T](t: Future[T]) def unwrap: T = Await.result(t, Duration.Inf)
