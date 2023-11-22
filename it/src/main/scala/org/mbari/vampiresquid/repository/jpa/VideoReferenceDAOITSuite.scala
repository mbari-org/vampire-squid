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

package org.mbari.vampiresquid.repository.jpa

import java.net.URI
import java.time.{Duration, Instant}
import org.mbari.vampiresquid.domain.Media
import org.mbari.vampiresquid.etc.jdk.Uris
import org.mbari.vampiresquid.repository.VideoReferenceDAO
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity
import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters.*
import scala.util.Random

trait VideoReferenceDAOITSuite extends BaseDAOSuite:

    given JPADAOFactory = daoFactory

    test("create"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequence                                  = TestUtils.build(1, 1, 1).head
        val videoReference                                 = videoSequence.getVideos.get(0).getVideoReferences.get(0)
        run(() => dao.create(videoReference))
        val videoEntity                                    = run(() => dao.findByUUID(videoReference.getUuid))
        assert(videoEntity.isDefined)
        dao.close()

    test("create should throw an exception if no parent video is assigned"):
        val vr = new VideoReferenceEntity(
            URI.create("http://foo.bar/someothervideo.mp4"),
            "video/mp4",
            "hevc",
            "pcm_s24le",
            1920,
            1080,
            30,
            Random.between(10000, 100000),
            "some description"
        )
        intercept[Exception] {
            given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
            run(() => dao.create(vr))
        }

    test("update"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequence                                  = TestUtils.create(1, 1, 1).head
        val videoReference                                 = videoSequence.getVideos.get(0).getVideoReferences.get(0)
        val videoCodec                                     = "video/av1"
        videoReference.setVideoCodec(videoCodec)
        run(() => dao.update(videoReference))
        val opt                                            = run(() => dao.findByUUID(videoReference.getUuid))
        assert(opt.isDefined)
        val entity                                         = opt.get
        assertEquals(entity.getVideoCodec, videoCodec)
        dao.close()

    test("delete"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequence                                  = TestUtils.create(1, 1, 1).head
        val videoReference                                 = videoSequence.getVideos.get(0).getVideoReferences.get(0)
        run(() =>
//      val entity = dao.update(videoReference) // Have to merge into persistent context before deleting
            dao.findByUUID(videoReference.getUuid) match
                case None         => fail("Failed to find videoReference with UUID = " + videoReference.getUuid)
                case Some(entity) => dao.delete(entity)
        )
        val opt                                            = run(() => dao.findByUUID(videoReference.getUuid))
        assert(opt.isEmpty)
        dao.close()

    test("deleteByUUID"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()

        val videoSequence  = TestUtils.create(1, 1, 1).head
        val videoReference = videoSequence.getVideos.get(0).getVideoReferences.get(0)
        run(() => dao.deleteByUUID(videoReference.getUuid))
        val opt            = run(() => dao.findByUUID(videoReference.getUuid))
        assert(opt.isEmpty)
        dao.close()

    test("findByUUID"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequence                                  = TestUtils.create(1, 1, 1).head
        val videoReference                                 = videoSequence.getVideos.get(0).getVideoReferences.get(0)
        val opt                                            = run(() => dao.findByUUID(videoReference.getUuid))
        assert(opt.isDefined)
        dao.close()

    test("findAll"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequence                                  = TestUtils.create(3, 3, 3).head
        val xs                                             = run(() => dao.findAll().toSeq)
        assertEquals(xs.size, 27)
        dao.close()

    test("findAllURIs"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequences                                 = TestUtils.create(3, 3, 3)
        val uris0                                          =
            videoSequences.flatMap(_.getVideos.asScala).flatMap(_.getVideoReferences.asScala).map(_.getUri).sorted
        val uris1                                          = run(() => dao.findAllURIs().toSeq.sorted)
        assertEquals(uris1.size, uris0.size)
        assertEquals(uris1, uris0)
        dao.close()

    test("findByVideoUUID"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequence                                  = TestUtils.create(1, 1, 1).head
        val video                                          = videoSequence.getVideos.get(0)
        val videoReference                                 = video.getVideoReferences.get(0)
        val xs                                             = run(() => dao.findByVideoUUID(video.getUuid))
        assertEquals(xs.size, 1)
        val entity                                         = xs.head
        assertEquals(entity.getUuid, videoReference.getUuid)
        assertEquals(entity.getUri, videoReference.getUri)
        dao.close()

    test("findConcurrent"):
        given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
        val vs                          = TestUtils.build(1, 29, 1).head
        val xs                          = vs.getVideoReferences.asScala
        val now                         = Instant.now()
        for (v, i) <- xs.zipWithIndex
        do
            val start    = now.plus(Duration.ofSeconds(100 * i))
            val duration = Duration.ofSeconds(500)
            v.getVideo.setStart(start)
            v.getVideo.setDuration(duration)
        run(() => dao.create(vs))(using dao)
        val m0 = Media.from(xs.head)
        dao.close()

//    println(s"--- Searching using ${master.getVideo.getStart}" )
        given vDao: VideoReferenceDAOImpl = daoFactory.newVideoReferenceDAO()
        val ys                            = run(() => vDao.findConcurrent(m0.video_reference_uuid.get))(using vDao)
        val good                          = xs.filter(_.getVideo().getStart().isBefore(m0.endTimestamp.get))
        assertEquals(ys.size, good.size)
        vDao.close()

    test("findByURI"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequence                                  = TestUtils.create(1, 3, 3).head
        val videoReference                                 = videoSequence.getVideoReferences.get(0)

        val opt    = run(() => dao.findByURI(videoReference.getUri))
        assert(opt.isDefined)
        val entity = opt.get
        assertEquals(entity.getUri, videoReference.getUri)
        dao.close()

    test("findByFileName"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()
        val videoSequence                                  = TestUtils.create(1, 3, 3).head
        val videoReference                                 = videoSequence.getVideoReferences.get(0)
        val filename                                       = Uris.filename(videoReference.getUri)

        val xs     = run(() => dao.findByFileName(filename))
        assertEquals(xs.size, 1)
        val entity = xs.head
        assertEquals(entity.getUri, videoReference.getUri)
        dao.close()

    test("findBySha512"):
        given dao: VideoReferenceDAO[VideoReferenceEntity] = daoFactory.newVideoReferenceDAO()

        val videoSequence  = TestUtils.create(1, 3, 3).head
        val videoReference = videoSequence.getVideoReferences.get(0)
        val sha            = videoReference.getSha512

        val opt    = run(() => dao.findBySha512(sha))
        assert(opt.isDefined)
        val entity = opt.get
        assertEquals(entity.getUri, videoReference.getUri)
        assertEquals(entity.getUuid, videoReference.getUuid)

        dao.close()
