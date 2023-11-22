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
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.{Duration, Instant}
import org.mbari.vampiresquid.AppConfig
import org.mbari.vampiresquid.controllers.MediaController
import org.mbari.vampiresquid.domain.{Media, MoveVideoParams}
import org.mbari.vampiresquid.etc.circe.CirceCodecs.{*, given}
import org.mbari.vampiresquid.etc.jdk.Logging
import org.mbari.vampiresquid.etc.jdk.Logging.given
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.sdk.FutureUtil.*
import org.mbari.vampiresquid.etc.sdk.Reflect
import org.mbari.vampiresquid.repository.jpa.TestDAOFactory
import scala.concurrent.{ExecutionContext, Future}
import sttp.client3.*
import sttp.client3.circe.*
import sttp.client3.SttpBackend
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import sttp.tapir.server.interceptor.CustomiseInterceptors
import sttp.tapir.server.stub.TapirStubInterpreter
import org.mbari.vampiresquid.etc.sdk.FormTransform.given
import org.mbari.vampiresquid.etc.sdk.ToStringTransforms.{*, given}
import org.mbari.vampiresquid.repository.jpa.TestUtils
import org.mbari.vampiresquid.etc.sdk.FutureUtil.given
import java.util.HexFormat
import org.mbari.vampiresquid.repository.jpa.AssertUtil.assertSameMedia
import sttp.tapir.server.ServerEndpoint
import org.mbari.vampiresquid.etc.jdk.Uris
import scala.jdk.CollectionConverters.*
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.repository.jpa.BaseDAOSuite
import scala.concurrent.ExecutionContext.Implicits.global

trait MediaEndpointsITSuite extends BaseDAOSuite:

    private val log = Logging(getClass)

    given JPADAOFactory             = daoFactory
    private val jwtService          = new JwtService("mbari", "foo", "bar")
    private lazy val controller     = new MediaController(daoFactory)
    private lazy val mediaEndpoints = new MediaEndpoints(controller, jwtService)

    test("createEndpointImpl - Create a new media using form body"):

        val jwt = jwtService.authorize("foo").orNull

        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(mediaEndpoints.createEndpointImpl)
            .backend()

        val now   = Instant.now()
        val media = new Media(
            video_sequence_name = Some("Test Dive 01"),
            video_name = Some("Test Dive 01 " + now),
            camera_id = Some("Tester 01"),
            uri = Some(URI.create("http://foo.org/v1/movie01.mp4")),
            start_timestamp = Some(now)
        )

        val request = basicRequest
            .post(uri"http://test.com/v1/media")
            .header("Authorization", s"Bearer $jwt")
            .body(Media.toFormMap(media))

        log.atDebug.log(request.toRfc2616Format(Set()))

        val response = request.send(backendStub)

        response
            .map(r =>
                println(s"--- ${r.body}")
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateEndpointImpl - Update an existing media using JSON body"):
        val jwt = jwtService.authorize("foo").orNull

        val now    = Instant.now()
        val media0 = new Media(
            video_sequence_name = Some("Test Dive 02"),
            video_name = Some("Test Dive 02 " + now),
            camera_id = Some("Tester 02"),
            uri = Some(URI.create("http://test.me/movie02.mp4")),
            start_timestamp = Some(now),
            sha512 = Some(Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
        )

        val media1 = controller.createMedia(media0).join
        val media2 = media1.copy(uri = Some(URI.create("http://test.me/movie02_changed.mp4")))

        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(mediaEndpoints.updateEndpointImpl)
            .backend()

        val request = basicRequest
            .put(uri"http://test.com/v1/media")
            .header("Authorization", s"Bearer $jwt")
            .body(Media.toFormMap(media2))

        log.atDebug.log(request.toRfc2616Format(Set()))

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateByVideoReferenceUuidEndpointImpl - Update an existing media using form body"):
        val jwt    = jwtService.authorize("foo").orNull
        val now    = Instant.now()
        val media0 = new Media(
            video_sequence_name = Some("Test Dive 03"),
            video_name = Some("Test Dive 03 " + now),
            camera_id = Some("Tester 03"),
            uri = Some(URI.create("http://test.me/movie03.mp4")),
            start_timestamp = Some(now),
            duration_millis = Some(30000),
            sha512 = Some(Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11))
        )

        val media1 = controller.createMedia(media0).join
        val media2 = media1.copy(uri = Some(URI.create("http://test.me/movie03_changed.mp4")))
        println(media2.stringify)

        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(mediaEndpoints.updateByVideoReferenceUuidEndpointImpl)
            .backend()

        val request = basicRequest
            .put(uri"http://test.com/v1/media/${media2.videoReferenceUuid}")
            .header("Authorization", s"Bearer $jwt")
            .body(Media.toFormMap(media2))

        log.atDebug.log(request.toRfc2616Format(Set()))

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
                val body = r.body.getOrElse(fail("no body was returned"))
                val e    = decode[Media](body)
                e match
                    case Left(e)  => fail("")
                    case Right(m) =>
                        assert(m.video_sequence_uuid.isDefined)
                        assert(m.video_uuid.isDefined)
                        assert(m.video_reference_uuid.isDefined)
                        assert(m.uri.isDefined)
                        assertEquals(media2.uri.get, m.uri.get)
            )
            .join

    test("moveByVideoReferenceUuidEndpointImpl"):
        val vs                                    = TestUtils.create(1, 1, 1).head
        val vr                                    = vs.getVideoReferences().get(0)
        val jwt                                   = jwtService.authorize("foo").orNull
        val mv                                    = MoveVideoParams("boogar", Instant.now(), 30000)
        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(mediaEndpoints.moveByVideoReferenceUuidEndpointImpl)
            .backend()
        val request                               = basicRequest
            .put(uri"http://test.com/v1/media/move/${vr.getUuid}")
            .header("Authorization", s"Bearer $jwt")
            .body(transform(mv))

        val response = request.send(backendStub)
        response.map(r =>
            assertEquals(r.code, StatusCode.Ok)
            assert(r.body.isRight)
            // TODO verify move happened
        )

        // TODO verify move to existing and move to new

    def checkResponse[T: Decoder](responseBody: Either[String, String]): T =
        responseBody match
            case Left(e)     => fail(e)
            case Right(json) =>
                decode[T](json) match
                    case Left(error)  => fail(error.getLocalizedMessage())
                    case Right(value) => value

    def runGet(
        ep: ServerEndpoint[Any, Future],
        uri: String,
        assertions: Response[Either[String, String]] => Unit
    ): Unit =
        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(ep)
            .backend()
        val request                               = basicRequest.get(uri"$uri")
        val response                              = request.send(backendStub).join
        assertions(response)

    test("findBySha512"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val hex            = HexFormat.of()
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        val sha512         = hex.formatHex(videoReference.getSha512())

        runGet(
            mediaEndpoints.findBySha512Impl,
            s"http://test.com/v1/media/sha512/${sha512}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val media = checkResponse[Media](response.body)
                assertSameMedia(media, media0)
        )

    test("findByVideoReferenceUuid"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(videoReference.getUuid() != null)

        runGet(
            mediaEndpoints.findByVideoReferenceUuidImpl,
            s"http://test.com/v1/media/videoreference/${videoReference.getUuid()}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val media = checkResponse[Media](response.body)
                assertSameMedia(media, media0)
        )

    test("findByFileName"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(media0.uri.isDefined)
        val filename       = Uris.filename(media0.uri.get)

        runGet(
            mediaEndpoints.findByFileNameImpl,
            s"http://test.com/v1/media/videoreference/filename/${filename}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findByVideoSequenceName"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)

        runGet(
            mediaEndpoints.findByVideoSequenceNameImpl,
            s"http://test.com/v1/media/videosequence/${videoSequence.getName()}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findByVideoName"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)

        runGet(
            mediaEndpoints.findByVideoNameImpl,
            s"http://test.com/v1/media/video/${videoReference.getVideo().getName()}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findByCameraIdAndTimestamps"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(media0.start_timestamp.isDefined)
        assert(media0.duration_millis.isDefined)
        val startTimestamp = media0.startTimestamp
        val endTimestamp   = startTimestamp.plus(media0.duration.get)

        runGet(
            mediaEndpoints.findByCameraIdAndTimestampsImpl,
            s"http://test.com/v1/media/camera/${videoSequence.getCameraID}/${startTimestamp}/${endTimestamp}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findConcurrentByVideoReferenceUuid"):
        val videoSequence = TestUtils.build(1, 10, 2).head
        var n             = 0
        val t0            = Instant.parse("1968-09-22T00:00:00Z")
        val t1            = Instant.parse("2002-08-27T00:00:00Z")
        for vr <- videoSequence.getVideos().asScala
        do
            if n < 5 then
                vr.setStart(t0.plus(Duration.ofSeconds(n * 100)))
                vr.setDuration(Duration.ofSeconds(1000))
            else
                vr.setStart(t1.plus(Duration.ofSeconds(n * 100)))
                vr.setDuration(Duration.ofSeconds(100))
            n = n + 1
        TestUtils.save(videoSequence)

        val befores = videoSequence.getVideoReferences().asScala.filter(_.getVideo().getStart().isBefore(t1))
        assertEquals(videoSequence.getVideoReferences().size(), 20)
        assertEquals(befores.size, 10)

        runGet(
            mediaEndpoints.findConcurrentByVideoReferenceUuidImpl,
            s"http://test.com/v1/media/concurrent/${videoSequence.getVideoReferences().get(0).getUuid()}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assertEquals(xs.size, befores.size)
        )

    test("findByCameraIdAndDatetime"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(media0.start_timestamp.isDefined)
        assert(media0.duration_millis.isDefined)
        val startTimestamp = media0.startTimestamp
        val dateTime       = startTimestamp.plus(media0.duration.get.dividedBy(2))

        runGet(
            mediaEndpoints.findByCameraIdAndDatetimeImpl,
            s"http://test.com/v1/media/camera/${videoSequence.getCameraID}/${dateTime}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findByUri"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(media0.uri.isDefined)
        val uri            = Uris.encode(media0.uri.get)

        runGet(
            mediaEndpoints.findByUriImpl,
            s"http://test.com/v1/media/uri/${uri}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )
