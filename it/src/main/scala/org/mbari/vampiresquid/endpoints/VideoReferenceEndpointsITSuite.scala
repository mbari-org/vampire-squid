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

import org.mbari.vampiresquid.controllers.VideoReferenceController
import org.mbari.vampiresquid.domain.{LastUpdatedTime, VideoReference, VideoReferenceCreate, VideoReferenceUpdate, VideoSequence}
import org.mbari.vampiresquid.etc.circe.CirceCodecs.*
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.etc.sdk.FutureUtil.*
import org.mbari.vampiresquid.etc.sdk.ToStringTransforms
import org.mbari.vampiresquid.repository.jpa.{AssertUtil, JPADAOFactory, TestUtils}
import sttp.client3.*
import sttp.model.StatusCode

import java.net.URI
import java.time.Instant
import java.util.{HexFormat, UUID}
import scala.concurrent.ExecutionContext

trait VideoReferenceEndpointsITSuite extends EndpointsSuite:

    given JPADAOFactory = daoFactory

    override def beforeAll(): Unit = daoFactory.beforeAll()
    override def afterAll(): Unit  = daoFactory.afterAll()

    given ExecutionContext       = ExecutionContext.global
    given jwtService: JwtService = new JwtService("mbari", "foo", "bar")
    lazy val controller          = new VideoReferenceController(daoFactory)
    lazy val endpoints           = new VideoReferenceEndpoints(controller)

    test("findAllVideoReferences"):
        val vrs             = TestUtils.create(2, 4, 1)
        val videoReferences = vrs.map(VideoSequence.from(_)).flatMap(_.videoReferences)
        runGet(
            endpoints.findAllVideoReferencesImpl,
            "http://test.com/v1/videoreferences",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[VideoReference]](response.body)
                assert(xs.size >= videoReferences.size) // some may be created in other tests
                for vr <- videoReferences
                do
                    xs.find(x => x.uuid == vr.uuid) match
                        case None    => fail(s"Could not find VideoReference with uuid = ${vr.uuid}")
                        case Some(x) => AssertUtil.assertSameVideoReference(x, vr)
        )

    test("findOneVideoReference"):
        val vrs = TestUtils.create(1, 1, 1).head
        val vr  = VideoSequence.from(vrs).videoReferences.head
        runGet(
            endpoints.findOneVideoReferenceImpl,
            s"http://test.com/v1/videoreferences/${vr.uuid}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val x = checkResponse[VideoReference](response.body)
                AssertUtil.assertSameVideoReference(x, vr)
        )

    test("findLastUpdateForVideoReference"):
        val vrs = TestUtils.create(1, 1, 1).head
        val vr  = VideoSequence.from(vrs).videoReferences.head
        runGet(
            endpoints.findLastUpdateForVideoReferenceImpl,
            s"http://test.com/v1/videoreferences/lastupdate/${vr.uuid}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val x = checkResponse[LastUpdatedTime](response.body)
                assert(x.timestamp.isBefore(Instant.now()))
        )

    test("findVideoReferenceByUri"):
        val vrs = TestUtils.create(1, 1, 1).head
        val vr  = VideoSequence.from(vrs).videoReferences.head
        val uri = uri"http://test.com/v1/videoreferences/uri/${vr.uri}" // have to escape the videoreferences uri
        runGet(
            endpoints.findVideoReferenceByUriImpl,
            uri.toString,
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val x = checkResponse[VideoReference](response.body)
                AssertUtil.assertSameVideoReference(x, vr)
        )

    test("findAllUris"):
        val vrs             = TestUtils.create(2, 4, 1)
        val videoReferences = vrs.map(VideoSequence.from(_)).flatMap(_.videoReferences)
        runGet(
            endpoints.findAllUrisImpl,
            "http://test.com/v1/videoreferences/uris",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val xs = checkResponse[List[URI]](response.body)
                assert(xs.size >= videoReferences.size) // some may be created in other tests
                for vr <- videoReferences
                do
                    xs.find(x => x == vr.uri) match
                        case None    => fail(s"Could not find VideoReference with uri = ${vr.uri}")
                        case Some(x) => assertEquals(x, vr.uri)
        )

    test("findVideoReferenceBySha512"):
        val vrs    = TestUtils.create(1, 1, 1).head
        val vr     = VideoSequence.from(vrs).videoReferences.head
        val sha512 = HexFormat.of().formatHex(vr.sha512.get)
        runGet(
            endpoints.findVideoReferenceBySha512Impl,
            s"http://test.com/v1/videoreferences/sha512/${sha512}",
            response =>
                assertEquals(response.code, StatusCode.Ok)
                val x = checkResponse[VideoReference](response.body)
                AssertUtil.assertSameVideoReference(x, vr)
        )

    test("deleteOneVideoReference"):
        val vrs         = TestUtils.create(1, 1, 1).head
        val vr          = VideoSequence.from(vrs).videoReferences.head
        val jwt         = jwtService.authorize("foo").orNull
        val backendStub = newBackendStub(endpoints.deleteOneVideoReferenceImpl)
        val request     = basicRequest
            .delete(uri"http://test.com/v1/videoreferences/${vr.uuid}")
            .header("Authorization", s"Bearer ${jwt}")
        val response    = request.send(backendStub).join
        assertEquals(response.code, StatusCode.NoContent)

    test("createOneVideoReference using form body"):
        val vrs   = TestUtils.create(1, 1, 1).head
        val video = vrs.getVideos().get(0)
        val jwt   = jwtService.authorize("foo").orNull

        val backendStub = newBackendStub(endpoints.createOneVideoReferenceImpl)

        val videoReference = new VideoReference(
            UUID.randomUUID(),
            new URI("http://yourvideo/is/here/foo.mp4"),
            Some("mp4"),
            Some("h264"),
            Some("aac"),
            Some(1920),
            Some(1080),
            Some(29.97),
            Some(1000000),
            Some(TestUtils.randomSha512()),
            Some("bar")
        )

        import org.mbari.vampiresquid.etc.sdk.FormTransform.given
        val form = ToStringTransforms.transform(videoReference) + "&video_uuid=" + video.getUuid().toString()

        val request = basicRequest
            .post(uri"http://test.com/v1/videoreferences")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(form)

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("createOneVideoReference using JSON body"):
        val vrs   = TestUtils.create(1, 1, 1).head
        val video = vrs.getVideos().get(0)
        val jwt   = jwtService.authorize("foo").orNull

        val backendStub = newBackendStub(endpoints.createOneVideoReferenceImpl)

        val videoReference = new VideoReferenceCreate(
            video.getUuid(),
            new URI("http://yourvideo/is/here/foodefafa.mp4"),
            Some("mp4"),
            Some("h264"),
            Some("aac"),
            Some(1920),
            Some(1080),
            Some(29.97),
            Some(1000000),
            Some(TestUtils.randomSha512()),
            Some("bar")
        )

        val request = basicRequest
            .post(uri"http://test.com/v1/videoreferences")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/json")
            .body(videoReference.stringify)

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateOneVideoReference using form body"):
        val vrs = TestUtils.create(1, 1, 1).head
        val vr  = vrs.getVideoReferences().get(0)
        val jwt = jwtService.authorize("foo").orNull

        val backendStub = newBackendStub(endpoints.updateOneVideoReferenceImpl)

        val videoReference = VideoReference.from(vr).copy(uri = new URI("http://yourvideo/is/now/here/foo.mp4"))
        import org.mbari.vampiresquid.etc.sdk.FormTransform.given
        val form           = ToStringTransforms.transform(videoReference)
        // println(form)

        val request = basicRequest
            .put(uri"http://test.com/v1/videoreferences/${vr.getUuid}")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(form)

        val response = request
            .send(backendStub)
            .map(r =>
                // println(r.body)
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
                val v1 = checkResponse[VideoReference](r.body)
                AssertUtil.assertSameVideoReference(videoReference, v1)
            )
            .join

    test("updateOneVideoReference using JSON body"):
        val vrs = TestUtils.create(1, 1, 1).head
        val vr  = vrs.getVideoReferences().get(0)
        val jwt = jwtService.authorize("foo").orNull

        val backendStub = newBackendStub(endpoints.updateOneVideoReferenceImpl)

        val videoReference = VideoReference.from(vr).copy(uri = new URI("http://yourvideo/is/now/here/foodebobo.mp4"))
        val update         = VideoReferenceUpdate.from(videoReference, Some(vr.getVideo().getUuid()))

        // println(form)

        val request = basicRequest
            .put(uri"http://test.com/v1/videoreferences/${vr.getUuid}")
            .header("Authorization", s"Bearer $jwt")
            .header("Content-Type", "application/json")
            .body(update.stringify)

        val response = request
            .send(backendStub)
            .map(r =>
                // println(r.body)
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
                val v1 = checkResponse[VideoReference](r.body)
                AssertUtil.assertSameVideoReference(videoReference, v1)
            )
            .join
