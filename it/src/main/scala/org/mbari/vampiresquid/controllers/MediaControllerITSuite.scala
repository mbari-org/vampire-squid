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

import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.junit.Assert
import org.mbari.vampiresquid.domain.Media
import org.mbari.vampiresquid.etc.jdk.Uris
import org.mbari.vampiresquid.repository.VideoReferenceDAO
import org.mbari.vampiresquid.repository.jpa.BaseDAOSuite
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity
import org.mbari.vampiresquid.repository.jpa.{TestUtils, VideoSequenceDAOImpl}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters.*

trait MediaControllerITSuite extends BaseDAOSuite:

    given JPADAOFactory = daoFactory

    override def beforeAll(): Unit = daoFactory.beforeAll()
    override def afterAll(): Unit  = daoFactory.afterAll()

    lazy val controller = MediaController(daoFactory)

    def buildMedia(): Media =
        val vs = TestUtils.build(1, 1, 1)
        val vr = vs.head.getVideoReferences().get(0)
        Media.from(vr)

    def createMedia(): Media =
        exec(controller.createMedia(buildMedia()))

    def assertSameValues(m0: Media, m1: Media): Unit =
        Assert.assertArrayEquals(m0.sha512.orNull, m1.sha512.orNull)
        assertEquals(m0.video_sequence_name, m1.video_sequence_name)
        assertEquals(m0.video_name, m1.video_name)
        assertEquals(m0.camera_id, m1.camera_id)
        // On a Github computer, comparing timestamps can fail as github actions
        // is returing presision out to value = 2024-02-14T17:37:16.568733220Z
        assertEquals(m0.start_timestamp.map(_.toEpochMilli()), m1.start_timestamp.map(_.toEpochMilli()))
        assertEquals(m0.duration, m1.duration)
        assertEquals(m0.uri, m1.uri)
        assertEquals(m0.container, m1.container)
        assertEquals(m0.video_codec, m1.video_codec)
        assertEquals(m0.audio_codec, m1.audio_codec)
        assertEquals(m0.width, m1.width)
        assertEquals(m0.height, m1.height)
        assertEquals(m0.frame_rate, m1.frame_rate)
        assertEquals(m0.size_bytes, m1.size_bytes)
        assertEquals(m0.description, m1.description)
        assertEquals(m0.video_description, m1.video_description)
        assertEquals(m0.video_sequence_description, m1.video_sequence_description)

    test("createMedia"):
        val m0 = buildMedia()
        val m1 = exec(controller.createMedia(m0))
        assertSameValues(m1, m0)

    test("create"):
        val m0 = buildMedia()
        val m1 = exec(
            controller.create(
                m0.video_sequence_name.get,
                m0.camera_id.get,
                m0.video_name.get,
                m0.uri.get,
                m0.start_timestamp.get,
                m0.duration,
                m0.container,
                m0.video_codec,
                m0.audio_codec,
                m0.width,
                m0.height,
                m0.frame_rate,
                m0.size_bytes,
                m0.description,
                m0.sha512,
                m0.video_sequence_description,
                m0.video_description
            )
        )
        assertSameValues(m1, m0)

    test("updateMedia"):
        val m0  = createMedia()
        val m1  = m0.copy(
            video_sequence_name = Some("foobarbaz"),
            video_name = Some("foobarbazbim"),
            video_codec = Some("video/magick")
        )
        val opt = exec(controller.updateMedia(m1))
        assert(opt.isDefined)
        val m2  = opt.get
        assertSameValues(m2, m1)

    test("findAndUpdate"):
        val m0                                                                               = createMedia()
        val m1                                                                               = m0.copy(
            video_codec = Some("video/magick"),
            video_sequence_description = Some("foobarbaz"),
            video_description = Some("foobarbazbim"),
            description = Some("foobarbazbimboom")
        )
        def find(dao: VideoReferenceDAO[VideoReferenceEntity]): Option[VideoReferenceEntity] =
            dao.findByUUID(m0.video_reference_uuid.get)
        val opt                                                                              = exec(
            controller.findAndUpdate(
                findFn = find,
                m1.video_sequence_name.get,
                m1.camera_id.get,
                m1.video_name.get,
                videoCodec = m1.video_codec,
                videoSequenceDescription = m1.video_sequence_description,
                videoDescription = m1.video_description,
                videoRefDescription = m1.description
            )
        )
        assert(opt.isDefined)
        val m2                                                                               = opt.get
        assertSameValues(m2, m1)

    test("update"):
        val m0  = createMedia()
        val m1  = m0.copy(
            video_codec = Some("video/magick"),
            video_sequence_description = Some("foobarbaz"),
            video_description = Some("foobarbazbim"),
            description = Some("foobarbazbimboom")
        )
        val opt = exec(
            controller.update(
                m1.sha512.get,
                m1.video_sequence_name.get,
                m1.camera_id.get,
                m1.video_name.get,
                videoCodec = m1.video_codec,
                videoSequenceDescription = m1.video_sequence_description,
                videoDescription = m1.video_description,
                videoRefDescription = m1.description
            )
        )
        assert(opt.isDefined)
        assertSameValues(opt.get, m1)

    test("moveVideoReference"):
        val m0 = createMedia()
        val m1 = createMedia()

        // move to existing
        val opt = exec(
            controller.moveVideoReference(
                m0.video_reference_uuid.get,
                m1.video_name.get,
                m1.start_timestamp.get,
                m1.duration.get
            )
        )
        assert(opt.isDefined)
        val m2  = opt.get
        assertEquals(m2.video_sequence_name, m1.video_sequence_name)
        assertEquals(m2.video_name, m1.video_name)
        assertEquals(m2.start_timestamp, m1.start_timestamp)
        assertEquals(m2.duration.get, m1.duration.get)

        // move to new
        val newName     = "one more ref"
        val newDuration = Duration.ofSeconds(100)
        val opt1        = exec(
            controller.moveVideoReference(
                m0.video_reference_uuid.get,
                newName,
                m1.start_timestamp.get,
                Duration.ofSeconds(100)
            )
        )
        assert(opt1.isDefined)
        val m3          = opt1.get
        assertEquals(m2.video_sequence_name, m1.video_sequence_name)
        assertEquals(m3.video_name.get, newName)
        assertEquals(m3.start_timestamp, m2.start_timestamp)
        assertEquals(m3.duration.get, newDuration)

        // move bogus uuid
        val newUuid = UUID.randomUUID()
        val opt2    =
            exec(controller.moveVideoReference(newUuid, newName, m1.start_timestamp.get, Duration.ofSeconds(100)))
        assert(opt2.isEmpty)

    test("findByVideoReferenceUuid"):
        val m0  = createMedia()
        val opt = exec(controller.findByVideoReferenceUuid(m0.video_reference_uuid.get))
        assert(opt.isDefined)
        assertSameValues(opt.get, m0)

    test("findBySha512"):
        val m0  = createMedia()
        val opt = exec(controller.findBySha512(m0.sha512.get))
        assert(opt.isDefined)
        assertSameValues(opt.get, m0)

    test("findByVideoSequenceName"):
        val m0 = createMedia()
        val xs = exec(controller.findByVideoSequenceName(m0.video_sequence_name.get))
        assertEquals(xs.size, 1)
        assertSameValues(xs.head, m0)

    test("findByVideoSequenceNames"):
        val xs = TestUtils.create(10, 1, 1).flatMap(vs => vs.getVideoReferences().asScala.map(Media.from))
        val ys = exec(controller.findByVideoSequenceNames(xs.map(_.video_sequence_name.get).toSet))
        assertEquals(ys.size, xs.size)

    test("findByVideoSequenceNameAndTimestamp"):
        val m0 = createMedia()
        val xs = exec(
            controller.findByVideoSequenceNameAndTimestamp(
                m0.video_sequence_name.get,
                m0.start_timestamp.get.plus(m0.duration.get.dividedBy(2))
            )
        )
        assertEquals(xs.size, 1)
        assertSameValues(xs.head, m0)

    test("findByCameraIdAndTimestamp"):
        val m0 = createMedia()
        val xs = exec(
            controller.findByCameraIdAndTimestamp(
                m0.camera_id.get,
                m0.start_timestamp.get.plus(m0.duration.get.dividedBy(2))
            )
        )
        assertEquals(xs.size, 1)
        assertSameValues(xs.head, m0)

    test("findByCameraIdAndTimestamps"):
        val m0 = createMedia()
        val xs =
            exec(
                controller.findByCameraIdAndTimestamps(
                    m0.camera_id.get,
                    m0.start_timestamp.get,
                    m0.start_timestamp.get.plus(m0.duration.get)
                )
            )
        assertEquals(xs.size, 1)
        assertSameValues(xs.head, m0)

    test("findConcurrent (simple case)"):
        val m0 = createMedia()
        val xs = exec(controller.findConcurrent(m0.video_reference_uuid.get))
        assertEquals(xs.size, 1)
        assertSameValues(xs.head, m0)
        // TODO create more than one concurrent video

    test("findConcurrent (advanced)"):
        val vs  = TestUtils.build(1, 10, 1).head
        val xs  = vs.getVideoReferences.asScala
        val now = Instant.now()
        for (v, i) <- xs.zipWithIndex
        do
            val start    = now.plus(Duration.ofSeconds(100 * i))
            val duration = Duration.ofSeconds(500)
            v.getVideo.setStart(start)
            v.getVideo.setDuration(duration)

        given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
        run(() => dao.create(vs))
        val m0                          = Media.from(xs.head)
        dao.close()

        val ys   = exec(controller.findConcurrent(m0.video_reference_uuid.get))
        val good = xs.filter(_.getVideo().getStart().isBefore(m0.endTimestamp.get))
        assertEquals(ys.size, good.size)
        assertSameValues(ys.head, m0)

    test("findByVideoName"):
        val m0 = createMedia()
        val xs = exec(controller.findByVideoName(m0.video_name.get))
        assertEquals(xs.size, 1)
        assertSameValues(xs.head, m0)

    test("findByURI"):
        val m0 = createMedia()
        val xs = exec(controller.findByURI(m0.uri.get))
        assertEquals(xs.size, 1)
        assertSameValues(xs.head, m0)

    test("findByFileName"):
        val m0       = createMedia()
        val filename = Uris.filename(m0.uri.get)
        val xs       = exec(controller.findByFileName(filename))
        assertEquals(xs.size, 1)
        assertSameValues(xs.head, m0)

    test("create with minimal arguments"):

        val videoSequenceName = getClass.getSimpleName() + "-1"

        val x = exec(
            controller.create(
                videoSequenceName,
                "Ventana",
                "V20160711T012345",
                URI.create("http://www.mbari.org/movies/airship.mp4"),
                Instant.parse("2016-07-11T01:23:45Z")
            )
        )

        val y = exec(controller.findByVideoSequenceName(videoSequenceName))
        assertEquals(y.size, 1)

    test("create when existing video sequence name is found"):
        val videoSequenceName = getClass.getSimpleName() + "-2"
        exec(
            controller.create(
                videoSequenceName,
                "Ventana",
                "V20160811T012345",
                URI.create("http://www.mbari.org/movies/airship-1.mp4"),
                Instant.parse("2016-08-11T01:23:45Z")
            )
        )
        exec(
            controller.create(
                videoSequenceName,
                "Ventana",
                "V20160812T012345",
                URI.create("http://www.mbari.org/movies/airship-2.mp4"),
                Instant.parse("2016-08-12T01:23:45Z")
            )
        )
        val y                 = exec(controller.findByVideoSequenceName(videoSequenceName))
        assertEquals(y.size, 2)

    test("create when existing video name is not found"):
        val videoSequenceName = getClass.getSimpleName() + "-3"
        exec(
            controller.create(
                videoSequenceName,
                "Ventana",
                "V20160811T012345",
                URI.create("http://www.mbari.org/movies/airship-3.mp4"),
                Instant.parse("2016-08-11T01:23:45Z")
            )
        )
        exec(
            controller.create(
                videoSequenceName,
                "Ventana",
                "V20160811T012345",
                URI.create("http://www.mbari.org/movies/airship-3-mezzanine.mp4"),
                Instant.parse("2016-08-11T01:23:45Z")
            )
        )

    test("create with all parameters provided"):
        val videoSequenceName = getClass.getSimpleName() + "-4"
        val x                 = exec(
            controller.create(
                videoSequenceName,
                "Ventana",
                "V20170911T012345",
                new URI("http://www.mbari.org/movies/airship_another.mp4"),
                Instant.parse("2017-08-11T01:23:45Z"),
                Some(Duration.ofMinutes(25)),
                Some("video/mp4"),
                Some("h264"),
                Some("aac"),
                Some(1920),
                Some(1080),
                Some(30),
                Some(12345678),
                Some("A test movie"),
                Some(TestUtils.randomSha512())
            )
        )
        val y                 = exec(controller.findByVideoSequenceName(videoSequenceName))
        assertEquals(y.size, 1)
        assertSameValues(y.head, x)
