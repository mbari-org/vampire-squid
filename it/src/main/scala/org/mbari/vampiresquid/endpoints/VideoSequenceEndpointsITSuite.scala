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

import java.util.UUID
import org.mbari.vampiresquid.controllers.VideoSequenceController
import org.mbari.vampiresquid.domain.{LastUpdatedTime, VideoSequence}
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import org.mbari.vampiresquid.etc.jdk.{Instants, Logging}
import org.mbari.vampiresquid.etc.jdk.Logging.given
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.sdk.FutureUtil.*
import org.mbari.vampiresquid.etc.sdk.FutureUtil.given
import org.mbari.vampiresquid.repository.jpa.{AssertUtil, JPADAOFactory, TestUtils}
import scala.concurrent.{ExecutionContext, Future}
import sttp.client3.*
import sttp.client3.SttpBackend
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import sttp.tapir.server.stub.TapirStubInterpreter

trait VideoSequenceEndpointsITSuite extends EndpointsSuite:

    private val log              = Logging(getClass)
    given JPADAOFactory          = daoFactory
    given ExecutionContext       = ExecutionContext.global
    given jwtService: JwtService = new JwtService("mbari", "foo", "bar")
    private lazy val controller  = new VideoSequenceController(daoFactory)
    private lazy val endpoints   = new VideoSequenceEndpoints(controller)

    test("findAllVideoSequences") {
        val videoSequence = TestUtils.create(1, 1, 1).head
        runGet(
            endpoints.findAllVideoSequencesImpl,
            "http://test.com/v1/videosequences",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[VideoSequence]](response.body)
                xs.find(vs => vs.uuid == videoSequence.getUuid) match
                    case Some(v1) =>
                        val v0 = VideoSequence.from(videoSequence)
                        AssertUtil.assertSameVideoSequence(v0, v1)
                    case None     => fail("Could not find all videosequences")
        )
    }

    test("findLastUpdateForVideoSequence"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        runGet(
            endpoints.findLastUpdateForVideoSequenceImpl,
            s"http://test.com/v1/videosequences/lastupdate/${videoSequence.getUuid}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[LastUpdatedTime](response.body)
                assert(xs.timestamp != null)
        )

    test("findVideoSequenceByName"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        runGet(
            endpoints.findVideoSequenceByNameImpl,
            s"http://test.com/v1/videosequences/name/${videoSequence.getName}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val v1 = checkResponse[VideoSequence](response.body)
                val v0 = VideoSequence.from(videoSequence)
                AssertUtil.deepAssertSameVideoSequence(v0, v1)
        )

    test("findAllVideoSequenceNames"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        runGet(
            endpoints.findAllVideoSequenceNamesImpl,
            s"http://test.com/v1/videosequences/names",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                response.body match
                    case Left(e)   => fail(s"Unable find all video sequence names: $e")
                    case Right(xs) => assert(xs.contains(videoSequence.getName))
        )

    test("findVideoSequenceNamesByCameraId"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        runGet(
            endpoints.findVideoSequenceNamesByCameraIdImpl,
            s"http://test.com/v1/videosequences/names/camera/${videoSequence.getCameraID}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                response.body match
                    case Left(e)   => fail(s"Unable find all video sequence names: $e")
                    case Right(xs) => assert(xs.contains(videoSequence.getName))
        )

    test("findAllCameraIds"):
        val videoSequences = TestUtils.create(4, 1, 1)
        runGet(
            endpoints.findAllCameraIdsImpl,
            s"http://test.com/v1/videosequences/cameras",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                response.body match
                    case Left(e)   => fail(s"Unable find all video sequence names: $e")
                    case Right(xs) =>
                        for vs <- videoSequences
                        do assert(xs.contains(vs.getCameraID))
        )

    test("findVideoSequencesByCameraId"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        runGet(
            endpoints.findVideoSequencesByCameraIdImpl,
            s"http://test.com/v1/videosequences/camera/${videoSequence.getCameraID}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[VideoSequence]](response.body)
                assertEquals(xs.size, 1)
                val v1 = VideoSequence.from(videoSequence)
                AssertUtil.deepAssertSameVideoSequence(xs.head, v1)
        )

    test("findVideoSequencesByCameraIdAndTimestamp"):
        val videoSequence = TestUtils.create(1, 3, 1).head
        val video         = videoSequence.getVideos.get(0)
        val t             = video.getStart.plus(video.getDuration.dividedBy(2))
        val ts            = Instants.CompactTimeFormatter.format(t)
        runGet(
            endpoints.findVideoSequencesByCameraIdAndTimestampImpl,
            s"http://test.com/v1/videosequences/camera/${videoSequence.getCameraID}/$ts",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[VideoSequence]](response.body)
                assertEquals(xs.size, 1)
                val v1 = VideoSequence.from(videoSequence)
                AssertUtil.deepAssertSameVideoSequence(xs.head, v1)
        )

    test("createOneVideoSequence"):

        val jwt = jwtService.authorize("foo").orNull

        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(endpoints.createOneVideoSequenceImpl)
            .backend()

        val videoSequence = new VideoSequence(UUID.randomUUID(), "foobarbaz", "brian's cam")
        val formMap       = VideoSequence.toFormMap(videoSequence)

        val request = basicRequest
            .post(uri"http://test.com/v1/videosequences")
            .header("Authorization", s"Bearer $jwt")
            .body(formMap)

        log.atDebug.log(request.toRfc2616Format(Set()))

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateOneVideoSequence"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull

        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(endpoints.updateOneVideoSequenceImpl)
            .backend()

        val v0      = VideoSequence
            .from(videoSequence)
            .copy(camera_id = "foogad", name = "brian was here", description = Some("testy tester"))
        val formMap = VideoSequence.toFormMap(v0)

        val request = basicRequest
            .put(uri"http://test.com/v1/videosequences/${videoSequence.getUuid}")
            .header("Authorization", s"Bearer $jwt")
            .body(formMap)

        log.atDebug.log(request.toRfc2616Format(Set()))

        val response = request.send(backendStub).join
        assertEquals(response.code, StatusCode.Ok)
        val v1       = checkResponse[VideoSequence](response.body)
        AssertUtil.deepAssertSameVideoSequence(v0, v1)

    test("deleteOneVideoSequence"):
        val videoSequence                         = TestUtils.create(1, 1, 1).head
        val jwt                                   = jwtService.authorize("foo").orNull
        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(endpoints.deleteOneVideoSequenceImpl)
            .backend()
        val request                               = basicRequest
            .delete(uri"http://test.com/v1/videosequences/${videoSequence.getUuid}")
            .header("Authorization", s"Bearer $jwt")
        val response                              = request.send(backendStub).join
        assertEquals(response.code, StatusCode.NoContent)
