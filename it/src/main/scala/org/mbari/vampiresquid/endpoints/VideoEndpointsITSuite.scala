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

import io.circe.generic.auto.*
import java.time.{Duration, Instant}
import java.util.UUID
import junit.extensions.TestDecorator
import org.mbari.vampiresquid.AppConfig
import org.mbari.vampiresquid.Endpoints.{*, given}
import org.mbari.vampiresquid.controllers.VideoController
import org.mbari.vampiresquid.controllers.VideoSequenceController
import org.mbari.vampiresquid.domain.Video
import org.mbari.vampiresquid.etc.circe.CirceCodecs.{*, given}
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.sdk.FutureUtil.join
import org.mbari.vampiresquid.repository.jpa.BaseDAOSuite
import org.mbari.vampiresquid.repository.jpa.DAOSuite
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.repository.jpa.TestDAOFactory
import org.mbari.vampiresquid.repository.jpa.TestUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters.*
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success
import sttp.client3.circe.*
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{basicRequest, UriContext}
import sttp.model.StatusCode
import sttp.tapir.model.StatusCodeRange.Success
import sttp.tapir.server.stub.TapirStubInterpreter

trait VideoEndpointsITSuite extends BaseDAOSuite:

    given JPADAOFactory = daoFactory

    given ExecutionContext = ExecutionContext.global

    given jwtService: JwtService = new JwtService("mbari", "foo", "bar")

    lazy val videoController         = new VideoController(daoFactory)
    lazy val videoSequenceController = new VideoSequenceController(daoFactory)
    lazy val videoEndpoints          = new VideoEndpoints(videoController, videoSequenceController)

    test("create"):

        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull
        assert(jwt != null)

        // given
        val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(videoEndpoints.createOneEndpointImpl)
            .backend()

        // when
        val response = basicRequest
            .post(uri"http://test.com/v1/videos")
            .header("Authorization", s"Bearer $jwt")
            .body(
                Map(
                    "name"                -> "test video",
                    "video_sequence_uuid" -> videoSequence.getUuid().toString,
                    "start"               -> Instant.now().toString,
                    "duration_millis"     -> "1000",
                    "description"         -> "test description"
                )
            )
            .send(backendStub)
            .join

        assertEquals(response.code, StatusCode.Ok)

    // response match {
    //   case Left(e) => fail(e.getMessage)
    //   case Right(r) =>
    //     assertEquals(r.code, StatusCode.Ok)
    //     r.body match
    //       case Left(e) => fail(e)
    //       case Right(b) =>
    //         b.reify[Video] match
    //           case Left(value) => fail(value.getLocalizedMessage())
    //           case Right(video) =>
    //             assertEquals(video.name, "test video")
    //             assert(video.description.isDefined)
    //             assertEquals(video.description.get, "test description")
    // }

    test("delete a video by UUID"):
        // given
        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull
        assert(jwt != null)

        val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(videoEndpoints.deleteByUuidEndpointImpl)
            .backend()

        // when
        val video    = videoSequence.getVideos.asScala.head
        val response = basicRequest
            .delete(uri"http://test.com/v1/videos/${video.getUuid}")
            .header("Authorization", s"Bearer $jwt")
            .send(backendStub)
            .join

        // then
        assertEquals(response.code, StatusCode.NoContent)

    test("update a video by UUID") {
        // given
        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull
        assert(jwt != null)

        val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(videoEndpoints.updateEndpointImpl)
            .backend()

        // when
        val video    = videoSequence.getVideos.asScala.head
        val response = basicRequest
            .put(uri"http://test.com/v1/videos/${video.getUuid}")
            .header("Authorization", s"Bearer $jwt")
            .body(
                Map(
                    "name"        -> "updated video",
                    "description" -> "updated description"
                )
            )
            .response(asJson[Video])
            .send(backendStub)
            .join

        // then
        assertEquals(response.code, StatusCode.Ok)
        //     response.map(_.body.value.name shouldBe "updated video").unwrap
        //     response.map(_.body.value.description shouldBe "updated description").unwrap
    }
