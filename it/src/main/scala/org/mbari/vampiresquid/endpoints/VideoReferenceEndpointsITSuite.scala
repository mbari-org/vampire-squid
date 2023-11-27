package org.mbari.vampiresquid.endpoints

import org.mbari.vampiresquid.repository.jpa.BaseDAOSuite
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.controllers.VideoReferenceController
import org.mbari.vampiresquid.repository.jpa.TestUtils
import scala.concurrent.ExecutionContext
import org.mbari.vampiresquid.domain.VideoSequence
import org.mbari.vampiresquid.repository.jpa.AssertUtil
import org.mbari.vampiresquid.etc.jwt.JwtService
import sttp.model.StatusCode
import org.mbari.vampiresquid.domain.VideoReference
import org.mbari.vampiresquid.etc.circe.CirceCodecs.given
import org.mbari.vampiresquid.domain.LastUpdatedTime
import java.time.Instant
import sttp.client3.*
import java.net.URI
import java.util.HexFormat
import org.mbari.vampiresquid.etc.sdk.FutureUtil.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.client3.testing.SttpBackendStub
import scala.concurrent.Future
import java.util.UUID
import org.mbari.vampiresquid.etc.sdk.ToStringTransforms
import sttp.tapir.server.vertx.VertxFutureServerOptions
import sttp.tapir.server.interceptor.exception.ExceptionHandler
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.server.interceptor.CustomiseInterceptors

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

    test("createOneVideoReference"):
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
            .body(form)

        val response = request.send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
            )
            .join

    test("updateOneVideoReference"):
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
            .body(form)

        val response = request
            .send(backendStub)
            .map(r =>
                // println(r.body)
                assertEquals(r.code, StatusCode.Ok)
                assert(r.body.isRight)
                val v1 = checkResponse[VideoReference](r.body)
                AssertUtil.assertSameVideoReference(v1, videoReference)
            )
            .join
