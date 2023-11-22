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

package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.jpa.DAOSuite
import org.mbari.vampiresquid.repository.jpa.TestUtils
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters.*
import org.mbari.vampiresquid.domain.Media
import org.mbari.vampiresquid.domain.VideoReference
import org.junit.Assert
import java.net.URI
import org.mbari.vampiresquid.repository.jpa.BaseDAOSuite
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory

trait VideoReferenceControllerITSuite extends BaseDAOSuite:

    given JPADAOFactory = daoFactory

    override def beforeAll(): Unit = daoFactory.beforeAll()
    override def afterAll(): Unit  = daoFactory.afterAll()

    lazy val controller = new VideoReferenceController(daoFactory)

    def assertSameValues(a: VideoReference, b: VideoReference): Unit =
        assertEquals(a.uri, b.uri)
        assertEquals(a.container, b.container)
        assertEquals(a.video_codec, b.video_codec)
        assertEquals(a.audio_codec, b.audio_codec)
        assertEquals(a.width, b.width)
        assertEquals(a.height, b.height)
        assertEquals(a.frame_rate, b.frame_rate)
        Assert.assertArrayEquals(a.sha512.orNull, b.sha512.orNull)
        assertEquals(a.size_bytes, b.size_bytes)
        assertEquals(a.description, b.description)

    test("findAll"):
        val vss = TestUtils.create(2, 4, 1)
        val vrs = vss.flatMap(_.getVideoReferences().asScala)
        val xs  = exec(controller.findAll())
        assert(xs.nonEmpty)
        assertEquals(xs.size, vrs.size)

    test("findAllURIs"):
        val vss = TestUtils.create(2, 4, 1)
        val vrs = vss.flatMap(_.getVideoReferences().asScala)
        val xs  = exec(controller.findAllURIs())
        assert(xs.nonEmpty)
        assertEquals(xs.size, vrs.size)

    test("findByUUID"):
        val vss = TestUtils.create(2, 4, 1)
        val vrs = vss.flatMap(_.getVideoReferences().asScala)
        val vr  = vrs.head
        val x   = exec(controller.findByUUID(vr.getUuid()))
        assert(x.isDefined)
        assertEquals(x.get.uuid, vr.getUuid)

    test("findByVideoUUID"):
        val vss            = TestUtils.create(1, 4, 2)
        val vrs            = vss.flatMap(_.getVideoReferences().asScala)
        val vr             = vrs.head
        val media          = Media.from(vr)
        val videoReference = VideoReference.from(vr)

        val x = exec(controller.findByVideoUUID(media.video_uuid.get))
        assert(x.nonEmpty)
        assertEquals(x.size, 2)

    test("findByURI"):
        val vss            = TestUtils.create(1, 4, 2)
        val vrs            = vss.flatMap(_.getVideoReferences().asScala)
        val vr             = vrs.head
        val videoReference = VideoReference.from(vr)

        val x = exec(controller.findByURI(vr.getUri()))
        assert(x.isDefined)
        assertSameValues(x.get, videoReference)

    test("findBySha512"):
        val vss            = TestUtils.create(1, 4, 2)
        val vrs            = vss.flatMap(_.getVideoReferences().asScala)
        val vr             = vrs.head
        val videoReference = VideoReference.from(vr)

        val x = exec(controller.findBySha512(vr.getSha512()))
        assert(x.isDefined)
        assertSameValues(x.get, videoReference)

    test("findConcurrent") {}

    test("create (minimum)"):
        val vss = TestUtils.create(1, 1, 1)
        val v   = vss.head.getVideos().get(0)
        val vr  = v.getVideoReferences().get(0)
        val x   = exec(controller.create(v.getUuid(), URI.create("http://foo.com/bar/baz.mp4")))
        assert(x.uuid != null)
        val y   = exec(controller.findByUUID(x.uuid))
        assert(y.isDefined)
        assertSameValues(x, y.get)

    test("create (full)"):
        val vss = TestUtils.create(1, 1, 1)
        val v   = vss.head.getVideos().get(0)
        val vr  = v.getVideoReferences().get(0)
        val x   = exec(
            controller.create(
                v.getUuid(),
                URI.create("http://foo.com/bar/baz.mp4"),
                Some("mp4"),
                Some("h264"),
                Some("aac"),
                Some(1920),
                Some(1080),
                Some(29.97),
                Some(123456789L),
                Some("This is a description"),
                Some(Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 0))
            )
        )
        assert(x.uuid != null)
        val y   = exec(controller.findByUUID(x.uuid))
        assert(y.isDefined)
        assertSameValues(x, y.get)

    test("update (minimum)"):
        val vss = TestUtils.create(1, 1, 1)
        val v   = vss.head.getVideos().get(0)
        val vr  = v.getVideoReferences().get(0)
        val x   = exec(controller.create(v.getUuid(), URI.create("http://foo.com/bar/baz.mp4")))
        assert(x.uuid != null)
        val y   = exec(controller.findByUUID(x.uuid))
        assert(y.isDefined)
        assertSameValues(x, y.get)

    test("update (full)"):
        val vss = TestUtils.create(1, 1, 1)
        val v   = vss.head.getVideos().get(0)
        val vr  = v.getVideoReferences().get(0)
        val x   = exec(
            controller.update(
                vr.getUuid(),
                Some(v.getUuid()),
                Some(URI.create("http://foo.com/barz/baz.mp4")),
                Some("mp4"),
                Some("h264"),
                Some("aac"),
                Some(1920),
                Some(1080),
                Some(29.97),
                Some(123456789L),
                Some("This is a description"),
                Some(Array[Byte](1, 2, 3, 4, 5, 6, 7, 8, 9, 0))
            )
        )
        assert(x.uuid != null)
        val y   = exec(controller.findByUUID(x.uuid))
        assert(y.isDefined)
        assertSameValues(x, y.get)

    test("delete"):
        val vss = TestUtils.create(1, 1, 1)
        val v   = vss.head.getVideos().get(0)
        val vr  = v.getVideoReferences().get(0)
        val w   = exec(controller.findByUUID(vr.getUuid()))
        assert(w.isDefined)
        val x   = exec(controller.delete(vr.getUuid()))
        assert(x)
        val y   = exec(controller.findByUUID(vr.getUuid()))
        assert(y.isEmpty)
