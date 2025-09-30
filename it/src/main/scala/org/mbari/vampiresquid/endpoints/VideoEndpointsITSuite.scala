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

import org.mbari.vampiresquid.controllers.{VideoController, VideoSequenceController}
import org.mbari.vampiresquid.domain.{LastUpdatedTime, Video, VideoUpdate}
import org.mbari.vampiresquid.etc.circe.CirceCodecs.{*, given}
import org.mbari.vampiresquid.etc.jdk.Instants
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.sdk.FutureUtil.join
import org.mbari.vampiresquid.repository.jpa.{AssertUtil, JPADAOFactory, TestUtils}
import sttp.client3.circe.*
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.model.StatusCode
import sttp.tapir.server.stub.TapirStubInterpreter

import java.time.Instant
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters.*

trait VideoEndpointsITSuite extends EndpointsSuite:

    given JPADAOFactory = daoFactory

    given ExecutionContext = ExecutionContext.global

    given jwtService: JwtService = new JwtService("mbari", "foo", "bar")

    lazy val videoController         = new VideoController(daoFactory)
    lazy val videoSequenceController = new VideoSequenceController(daoFactory)
    lazy val videoEndpoints          = new VideoEndpoints(videoController, videoSequenceController)

    test("findAllVideos"):
        val videoSequence = TestUtils.create(1, 4, 1).head
        val video         = videoSequence.getVideos.get(0)
        runGet(
            videoEndpoints.findAllVideosImpl,
            "http://test.com/v1/videos",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val videos = checkResponse[List[Video]](response.body)
                assert(videos.nonEmpty)
                assertEquals(videos.size, videoSequence.getVideos.size)
        )
        runGet(
            videoEndpoints.findAllVideosImpl,
            "http://test.com/v1/videos?limit=2&offset=1",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val videos = checkResponse[List[Video]](response.body)
                assert(videos.nonEmpty)
                assertEquals(videos.size, 2)
        )

    test("findOne"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        val video         = Video.from(videoSequence.getVideos.asScala.head)

        runGet(
            videoEndpoints.findOneVideoImpl,
            s"http://test.com/v1/videos/${video.uuid}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val video1 = checkResponse[Video](response.body)
                AssertUtil.assertSameVideo(video1, video)
        )

    test("findVideoByVideoSequenceUuid"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        val video         = Video.from(videoSequence.getVideos.asScala.head)
        runGet(
            videoEndpoints.findVideoByVideoSequenceUuidImpl,
            s"http://test.com/v1/videos/videosequence/${videoSequence.getUuid}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val video1 = checkResponse[List[Video]](response.body).head
                AssertUtil.assertSameVideo(video1, video)
        )

    test("findVideoByVideoReferenceUuid"):
        val videoSequence  = TestUtils.create(1, 1, 1).head
        val video          = Video.from(videoSequence.getVideos.get(0))
        val videoReference = video.videoReferences.head
        runGet(
            videoEndpoints.findVideoByVideoReferenceUuidImpl,
            s"http://test.com/v1/videos/videoreference/${videoReference.uuid}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val video1 = checkResponse[Video](response.body)
                AssertUtil.assertSameVideo(video1, video)
        )

    test("findLastUpdateForVideo"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        val video         = Video.from(videoSequence.getVideos.get(0))
        runGet(
            videoEndpoints.findLastUpdateForVideoImpl,
            s"http://test.com/v1/videos/lastupdate/${video.uuid}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val lut = checkResponse[LastUpdatedTime](response.body)
                assertEquals(video.last_updated_time.getOrElse(Instant.EPOCH), lut.timestamp)
        )

    test("findVideoByName"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        val video         = Video.from(videoSequence.getVideos.get(0))
        runGet(
            videoEndpoints.findVideoByNameImpl,
            s"http://test.com/v1/videos/name/${video.name}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val video1 = checkResponse[List[Video]](response.body).head
                AssertUtil.assertSameVideo(video1, video)
        )

    test("findVideoByVideoSequenceName"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        val video         = Video.from(videoSequence.getVideos.get(0))
        runGet(
            videoEndpoints.findVideoByVideoSequenceByNameImpl,
            s"http://test.com/v1/videos/videosequence/name/${videoSequence.getName}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val video1 = checkResponse[List[Video]](response.body).head
                AssertUtil.assertSameVideo(video1, video)
        )

    test("findVideoByTimestamp"):
        val videoSequence = TestUtils.create(1, 1, 1).head
        val video         = Video.from(videoSequence.getVideos.get(0))
        val ts            = video.start_timestamp
        val xs            = List(
            Instants.TimeFormatter.format(ts),
            Instants.CompactTimeFormatter.format(ts),
            Instants.CompactTimeFormatterMs.format(ts),
            Instants.CompactTimeFormatterNs.format(ts)
        )
        for x <- xs
        do
            runGet(
                videoEndpoints.findVideoByTimestampImpl,
                s"http://test.com/v1/videos/timestamp/${x}",
                response =>
                    assertEquals(response.code, StatusCode.Ok)
                    val video1 = checkResponse[List[Video]](response.body).head
                    AssertUtil.assertSameVideo(video1, video)
            )

    test("findVideoByTimestampRange"):
        val videoSequence = TestUtils.create(1, 4, 1).head
        val minVideo      = videoSequence.getVideos.asScala.minBy(_.getStart.toEpochMilli)
        val maxVideo      = videoSequence.getVideos.asScala.maxBy(_.getStart.toEpochMilli)
        val ts            = List(minVideo.getStart, maxVideo.getStart)
        val xs            = List(
            ts.map(Instants.TimeFormatter.format),
            ts.map(Instants.CompactTimeFormatter.format),
            ts.map(Instants.CompactTimeFormatterMs.format),
            ts.map(Instants.CompactTimeFormatterNs.format)
        )
        for x <- xs
        do
            runGet(
                videoEndpoints.findVideoByTimestampRangeImpl,
                s"http://test.com/v1/videos/timestamp/${x(0)}/${x(1)}",
                response =>
                    assertEquals(response.code, StatusCode.Ok)
                    val videos = checkResponse[List[Video]](response.body)
                    assertEquals(videos.size, 4)
            )

    test("createOne"):

        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull
        assert(jwt != null)

        // given
        val backendStub = newBackendStub(videoEndpoints.createOneVideoImpl)

        // when
        val response = basicRequest
            .post(uri"http://test.com/v1/videos")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/x-www-form-urlencoded")
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

    test("deleteVideoByUuid"):
        // given
        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull
        assert(jwt != null)

        val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(videoEndpoints.deleteVideoByUuidImpl)
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

    test("updateVideo - form body") {
        // given
        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull
        assert(jwt != null)

        val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(videoEndpoints.updateVideoImpl)
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

    test("updateVideo - json body") {
        // given
        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull
        assert(jwt != null)

        val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(videoEndpoints.updateVideoImpl)
            .backend()

        // when
        val video       = videoSequence.getVideos.asScala.head
        val videoUpdate = VideoUpdate(
            name = Some("updated video"),
            description = Some("updated description"),
            start_timestamp = Some(Instant.now())
        )
        val response    = basicRequest
            .put(uri"http://test.com/v1/videos/${video.getUuid}")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/json")
            .body(videoUpdate.stringify)
            .response(asJson[Video])
            .send(backendStub)
            .join

        // then
        assertEquals(response.code, StatusCode.Ok)
    }

    test("updateVideo - form body - update only start_timestamp") {
        // given
        val videoSequence = TestUtils.create(1, 1, 1).head
        val jwt           = jwtService.authorize("foo").orNull
        assert(jwt != null)

        val backendStub = TapirStubInterpreter(SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(videoEndpoints.updateVideoImpl)
            .backend()

        // when
        val video       = videoSequence.getVideos.asScala.head
        val videoUpdate = VideoUpdate(
            start_timestamp = Some(Instant.now())
        )
        val response    = basicRequest
            .put(uri"http://test.com/v1/videos/${video.getUuid}")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/json")
            .body(videoUpdate.stringify)
            .response(asJson[Video])
            .send(backendStub)
            .join

        // then
        assertEquals(response.code, StatusCode.Ok)
        response.body match
            case Left(e)  => fail(e.getMessage())
            case Right(b) =>
                assertEquals(b.start_timestamp, videoUpdate.start_timestamp.get)

    }
