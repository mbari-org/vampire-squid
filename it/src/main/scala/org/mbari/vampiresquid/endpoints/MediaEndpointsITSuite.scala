/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
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
import java.time.{Duration, Instant}
import java.util.HexFormat
import org.mbari.vampiresquid.controllers.MediaController
import org.mbari.vampiresquid.domain.{Media, MoveVideoParams}
import org.mbari.vampiresquid.etc.circe.CirceCodecs.{*, given}
import org.mbari.vampiresquid.etc.jdk.Logging
import org.mbari.vampiresquid.etc.jdk.Logging.given
import org.mbari.vampiresquid.etc.jdk.Uris
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.sdk.FormTransform.given
import org.mbari.vampiresquid.etc.sdk.FutureUtil.*
import org.mbari.vampiresquid.etc.sdk.ToStringTransforms.{*, given}
import org.mbari.vampiresquid.repository.jpa.AssertUtil.assertSameMedia
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.repository.jpa.TestUtils
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*
import sttp.client3.*
import sttp.client3.SttpBackend
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import sttp.tapir.server.stub.TapirStubInterpreter
import org.junit.Assert.*

trait MediaEndpointsITSuite extends EndpointsSuite:

    private val log = Logging(getClass)

    given JPADAOFactory             = daoFactory
    private val jwtService          = new JwtService("mbari", "foo", "bar")
    private lazy val controller     = new MediaController(daoFactory)
    private lazy val mediaEndpoints = new MediaEndpoints(controller, jwtService)

    test("createMedia - Create a new media using form body"):

        val jwt         = jwtService.authorize("foo").orNull
        val backendStub = newBackendStub(mediaEndpoints.createMediaImpl)

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
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(Media.toFormMap(media))

        log.atDebug.log(request.toRfc2616Format(Set()))

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("createMedia - Create a new media using JSON body"):

        val jwt         = jwtService.authorize("foo").orNull
        val backendStub = newBackendStub(mediaEndpoints.createMediaImpl)

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
            .header("Content-Type", "application/json")
            .body(media.stringify)

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateMedia - Update an existing media's start timestamp using form body"):
        val jwt         = jwtService
            .authorize("foo")
            .orNull
        val now         = Instant.now()
        val media0      = new Media(
            video_sequence_name = Some("Test Dive 20250304"),
            video_name = Some("Test Dive 20250304 " + now),
            camera_id = Some("Tester 03"),
            uri = Some(URI.create("http://test.me/movie20250304.mp4")),
            start_timestamp = Some(now),
            sha512 = Some(Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 23))
        )
        val media1      = controller.createMedia(media0).join
        assert(media1.video_reference_uuid.isDefined)
        assert(media1.video_sequence_name.isDefined)
        assert(media1.camera_id.isDefined)
        assert(media1.video_name.isDefined)
        // NOTE: The following fields are all required to be set
        //       in order to update the media
        val media2      = Media(
            video_sequence_name = media1.video_sequence_name,
            camera_id = media1.camera_id,
            video_name = media1.video_name,
            start_timestamp = Some(now.plusSeconds(1000))
        )
        val backendStub = newBackendStub(mediaEndpoints.updateMediaByVideoReferenceUuidImpl)
        val request     = basicRequest
            .put(uri"http://test.com/v1/media/${media1.videoReferenceUuid}")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(Media.toFormMap(media2))
        log.atDebug.log(request.toRfc2616Format(Set()))
        val response    = request.send(backendStub)
        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateMedia - Update an existing media using form body"):
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

        val backendStub = newBackendStub(mediaEndpoints.updateMediaImpl)

        val request = basicRequest
            .put(uri"http://test.com/v1/media")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(Media.toFormMap(media2))

        log.atDebug.log(request.toRfc2616Format(Set()))

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateMedia - Update an existing media using JSON body"):
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

        val backendStub = newBackendStub(mediaEndpoints.updateMediaImpl)

        val request = basicRequest
            .put(uri"http://test.com/v1/media")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/json")
            .body(media2.stringify)

        log.atDebug.log(request.toRfc2616Format(Set()))

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateMediaByVideoReferenceUuid - Update an existing media using form body"):
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

        val backendStub = newBackendStub(mediaEndpoints.updateMediaByVideoReferenceUuidImpl)

        val request = basicRequest
            .put(uri"http://test.com/v1/media/${media2.videoReferenceUuid}")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/x-www-form-urlencoded")
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

    test("updateMediaByVideoReferenceUuid - Update an existing media using JSON body"):
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
//        println(media2.stringify)

        val backendStub = newBackendStub(mediaEndpoints.updateMediaByVideoReferenceUuidImpl)

        val request = basicRequest
            .put(uri"http://test.com/v1/media/${media2.videoReferenceUuid}")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/json")
            .body(media2.stringify)

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

    test("moveMediaByVideoReferenceUuidImpl"):
        val vs                                    = TestUtils.create(1, 1, 1).head
        val vr                                    = vs.getVideoReferences().get(0)
        val jwt                                   = jwtService.authorize("foo").orNull
        val mv                                    = MoveVideoParams("boogar", Instant.now(), 30000)
        val backendStub: SttpBackend[Future, Any] = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(mediaEndpoints.moveMediaByVideoReferenceUuidImpl)
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

    test("findMediaBySha512"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val hex            = HexFormat.of()
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        val sha512         = hex.formatHex(videoReference.getSha512())

        runGet(
            mediaEndpoints.findMediaBySha512Impl,
            s"http://test.com/v1/media/sha512/${sha512}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val media = checkResponse[Media](response.body)
                assertSameMedia(media, media0)
        )

    test("findMediaByVideoReferenceUuid"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(videoReference.getUuid() != null)

        runGet(
            mediaEndpoints.findMediaByVideoReferenceUuidImpl,
            s"http://test.com/v1/media/videoreference/${videoReference.getUuid()}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val media = checkResponse[Media](response.body)
                assertSameMedia(media, media0)
        )

    test("findMediaByFileName"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(media0.uri.isDefined)
        val filename       = Uris.filename(media0.uri.get)

        runGet(
            mediaEndpoints.findMediaByFileNameImpl,
            s"http://test.com/v1/media/videoreference/filename/${filename}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findMediaByVideoSequenceName"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences.get(0)
        val media0         = Media.from(videoReference)

        runGet(
            mediaEndpoints.findMediaByVideoSequenceNameImpl,
            s"http://test.com/v1/media/videosequence/${videoSequence.getName}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findMediaByVideoSequenceNames"):
        val videoSequences = TestUtils.create(2, 2, 2)
        val names          = videoSequences.map(_.getName())

        val backendStub = newBackendStub(mediaEndpoints.findMediaByVideoSequenceNamesImpl)

        // -- Request all
        val request = basicRequest
            .post(uri"http://test.com/v1/media/videosequence")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(names.stringify)

        val response = request.send(backendStub)
        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
                val media        = checkResponse[List[Media]](r.body)
                val expectedSize = videoSequences
                    .map(_.getVideoReferences().size())
                    .sum
                assertEquals(expectedSize, media.size)
            )
            .join

        // -- Request page
        val request1 = basicRequest
            .post(uri"http://test.com/v1/media/videosequence?offset=1&limit=2")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(names.stringify)

        val response1 = request1.send(backendStub)
        response1
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
                val media = checkResponse[List[Media]](r.body)
                assertEquals(media.size, 2)
            )
            .join

    test("findMediaByVideoName"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)

        runGet(
            mediaEndpoints.findMediaByVideoNameImpl,
            s"http://test.com/v1/media/video/${videoReference.getVideo().getName()}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findMediaByCameraIdAndTimestamps"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(media0.start_timestamp.isDefined)
        assert(media0.duration_millis.isDefined)
        val startTimestamp = media0.startTimestamp
        val endTimestamp   = startTimestamp.plus(media0.duration.get)

        runGet(
            mediaEndpoints.findMediaByCameraIdAndTimestampsImpl,
            s"http://test.com/v1/media/camera/${videoSequence.getCameraID}/${startTimestamp}/${endTimestamp}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findConcurrentMediaByVideoReferenceUuid"):
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
            mediaEndpoints.findConcurrentMediaByVideoReferenceUuidImpl,
            s"http://test.com/v1/media/concurrent/${videoSequence.getVideoReferences().get(0).getUuid()}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assertEquals(xs.size, befores.size)
        )

    test("findMediaByCameraIdAndDatetime"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(media0.start_timestamp.isDefined)
        assert(media0.duration_millis.isDefined)
        val startTimestamp = media0.startTimestamp
        val dateTime       = startTimestamp.plus(media0.duration.get.dividedBy(2))

        runGet(
            mediaEndpoints.findMediaByCameraIdAndDatetimeImpl,
            s"http://test.com/v1/media/camera/${videoSequence.getCameraID}/${dateTime}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[Media]](response.body)
                assert(xs.size == 1)
                assertSameMedia(xs.head, media0)
        )

    test("findMediaByUri"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideoReferences().get(0)
        val media0         = Media.from(videoReference)
        assert(media0.uri.isDefined)
        val uri            = Uris.encode(media0.uri.get)

        runGet(
            mediaEndpoints.findMediaByUriImpl,
            s"http://test.com/v1/media/uri/${uri}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[Media](response.body)
                assertSameMedia(xs, media0)
        )
