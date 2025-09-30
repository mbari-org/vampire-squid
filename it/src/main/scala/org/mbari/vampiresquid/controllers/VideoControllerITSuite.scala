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

package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.domain.Video
import org.mbari.vampiresquid.repository.jpa.{BaseDAOSuite, JPADAOFactory, TestUtils}

import java.time.{Duration, Instant}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters.*

trait VideoControllerITSuite extends BaseDAOSuite:

    given JPADAOFactory = daoFactory

    override def beforeAll(): Unit = daoFactory.beforeAll()
    override def afterAll(): Unit  = daoFactory.afterAll()

    lazy val controller = new VideoController(daoFactory)

    private def assertSameValues(a: Video, b: Video): Unit =
        assertEquals(a.name, b.name)
        assertEquals(a.description, b.description)
        assertEquals(a.start_timestamp, b.start_timestamp)
        assertEquals(a.duration, b.duration)

    test("findAll"):
        val vss    = TestUtils.create(2, 4, 1)
        val videos = vss.flatMap(_.getVideos().asScala)
        val xs     = exec(controller.findAll(0, 10000))
        assertEquals(xs.size, videos.size)

    test("findByUUID"):
        val vs    = TestUtils.create(1, 4, 1).head
        val video = vs.getVideos().get(0)
        val x     = exec(controller.findByUUID(video.getUuid()))
        assert(x.isDefined)
        assertSameValues(Video.from(video), x.get)

    test("findVideoSequenceByVideoUuid"):
        val vss    = TestUtils.create(2, 4, 1)
        val videos = vss.flatMap(_.getVideos().asScala)
        val x      = exec(controller.findVideoSequenceByVideoUuid(videos.head.getUuid()))
        assert(x.isDefined)
        assertEquals(x.get.uuid, vss.head.getUuid)

    test("findAllNames"):
        val vss    = TestUtils.create(2, 4, 1)
        val videos = vss.flatMap(_.getVideos().asScala)
        val xs     = exec(controller.findAllNames())
        assertEquals(xs.size, videos.size)

    test("findAllNamesAndTimestamps"):
        val vss    = TestUtils.create(2, 4, 1)
        val videos = vss.flatMap(_.getVideos().asScala)
        val xs     = exec(controller.findAllNamesAndTimestamps())
        assertEquals(xs.size, videos.size)

    test("findBetweenTimestamps"):
        val vss    = TestUtils.create(2, 4, 1)
        val videos = vss.flatMap(_.getVideos().asScala).sortBy(_.getStart())
        val t0     = videos.head.getStart()
        val t1     = videos.init.last.getStart()
        val xs     = exec(controller.findBetweenTimestamps(t0, t1))
        assertEquals(xs.size, videos.size - 1)

    test("findByTimestamp"):
        val vss    = TestUtils.create(2, 4, 1)
        val videos = vss.flatMap(_.getVideos().asScala).sortBy(_.getStart())
        val v      = videos.head
        val t0     = v.getStart().plus(v.getDuration().dividedBy(2))
        val xs     = exec(controller.findByTimestamp(t0))
        assertEquals(xs.size, 1)

    test("findByVideoReferenceUUID"):
        val vss    = TestUtils.create(2, 4, 1)
        val videos = vss.flatMap(_.getVideos().asScala)
        val video  = videos.head
        val vr     = video.getVideoReferences().get(0)
        val x      = exec(controller.findByVideoReferenceUUID(vr.getUuid()))
        assert(x.isDefined)
        assertSameValues(Video.from(video), x.get)

    test("findByVideoSequenceUUID"):
        val vss    = TestUtils.create(2, 4, 1).head
        val videos = vss.getVideos().asScala
        val video  = videos.head
        val vs     = video.getVideoSequence
        val xs     = exec(controller.findByVideoSequenceUUID(vs.getUuid()))
        assertEquals(xs.size, videos.size)

    test("findByName"):
        val vss    = TestUtils.create(2, 4, 1)
        val videos = vss.flatMap(_.getVideos().asScala)
        val video  = videos.head
        val x      = exec(controller.findByName(video.getName()))
        assert(x.isDefined)
        assertSameValues(Video.from(video), x.get)

    test("findNamesByVideoSequenceName"):
        val vss    = TestUtils.create(2, 4, 1).head
        val videos = vss.getVideos().asScala
        val video  = videos.head
        val vs     = video.getVideoSequence
        val xs     = exec(controller.findNamesByVideoSequenceName(vs.getName))
        assertEquals(xs.size, videos.size)

    test("create"):
        val vs          = TestUtils.create(1, 4, 1).head
        val name        = "foo"
        val now         = Instant.now()
        val duration    = Some(Duration.ofSeconds(10))
        val description = Some("hello world")
        val video       = exec(controller.create(vs.getUuid, name, now, duration, description))
        assertEquals(video.name, name)
        assertEquals(video.start_timestamp, now)
        assertEquals(video.duration, duration)
        assertEquals(video.description, description)
        val opt         = exec(controller.findByUUID(video.uuid))
        assert(opt.isDefined)
        val video2      = opt.get
        assertEquals(video2.name, video.name)
        assertEquals(video2.duration_millis, video.duration_millis)
        assertEquals(video2.description, video.description)
        // The db rounds timestamps to the nearest millisecond,
        // so we can't compare them directly

    test("update"):
        val vs          = TestUtils.create(1, 4, 1).head
        val video       = vs.getVideos().get(0)
        val name        = "foo"
        val now         = Instant.now()
        val duration    = Some(Duration.ofSeconds(10))
        val description = Some("hello world")
        val video2      = exec(controller.update(video.getUuid(), Some(name), Some(now), duration, description))
        assertEquals(video2.name, name)
        assertEquals(video2.start_timestamp, now)
        assertEquals(video2.duration, duration)
        assertEquals(video2.description, description)
        val opt         = exec(controller.findByUUID(video2.uuid))
        assert(opt.isDefined)
        val video3      = opt.get
        assertEquals(video3.name, video2.name)
        assertEquals(video3.duration_millis, video2.duration_millis)
        assertEquals(video3.description, video2.description)
        // The db rounds timestamps to the nearest millisecond,
        // so we can't compare them directly

    test("delete"):
        val vs      = TestUtils.create(1, 4, 1).head
        val video   = vs.getVideos().get(0)
        val uuid    = video.getUuid()
        val opt     = exec(controller.findByUUID(uuid))
        assert(opt.isDefined)
        val deleted = exec(controller.delete(uuid))
        assert(deleted)
        val opt2    = exec(controller.findByUUID(uuid))
        assert(opt2.isEmpty)
