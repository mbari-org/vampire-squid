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

import org.mbari.vampiresquid.repository.{VideoReferenceDAO, VideoSequenceDAO}
import org.mbari.vampiresquid.repository.jpa.entity.{VideoReferenceEntity, VideoSequenceEntity}

import java.util.UUID
import scala.jdk.CollectionConverters.*

class VideoSequenceDAOSuite extends DAOSuite:

  test("create"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.build(1, 1, 1).head
    run(() => dao.create(videoSequence))
    val videoEntity = run(() => dao.findByUUID(videoSequence.getUuid))
    assert(videoEntity.isDefined)
    dao.close()

  test("update"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val newName = "foobarbazbim"
    videoSequence.setName(newName)
    run(() => dao.update(videoSequence))
    val opt = run(() => dao.findByUUID(videoSequence.getUuid))
    assert(opt.isDefined)
    val entity = opt.get
    assertEquals(entity.getName, newName)
    dao.close()

  test("update cascades to videos"):

    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video0 = videoSequence.getVideos.asScala.minBy(_.getStart)
    val newName = "foobarbaz__"
    video0.setName(newName)
    run(() => dao.update(videoSequence))
    val opt = run(() => dao.findByUUID(videoSequence.getUuid))
    assert(opt.isDefined)
    val entity = opt.get
    val video1 = entity.getVideos.asScala.minBy(_.getStart)
    assertEquals(video1.getName, newName)
    dao.close()

  test("update cascades to videoReferences"):

    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val videoReference0 = videoSequence.getVideoReferences.asScala.minBy(_.getUri.toString)
    val newVideoCodec = "video/magic"
    videoReference0.setVideoCodec(newVideoCodec)
    run(() => dao.update(videoSequence))
    val opt = run(() => dao.findByUUID(videoSequence.getUuid))
    assert(opt.isDefined)
    val entity = opt.get
    val videoReference1 = entity.getVideoReferences.asScala.minBy(_.getUri.toString)
    assertEquals(videoReference0.getVideoCodec, videoReference1.getVideoCodec)
    dao.close()

  test("delete"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    run(() => {
      val entity = dao.update(videoSequence) // Have to merge into persistent context before deleting
      dao.delete(entity)
    })
    val opt = run(() => dao.findByUUID(videoSequence.getUuid))
    assert(opt.isEmpty)
    dao.close()

  test("deleteByUUID"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    run(() => dao.deleteByUUID(videoSequence.getUuid))
    val opt = run(() => dao.findByUUID(videoSequence.getUuid))
    assert(opt.isEmpty)
    dao.close()

  test("findByUUID"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val opt = run(() => dao.findByUUID(videoSequence.getUuid))
    assert(opt.isDefined)

    // try with a random uuid
    val emptyOpt = run(() => dao.findByUUID(UUID.randomUUID()))
    assert(emptyOpt.isEmpty)
    dao.close()

  test("findAll"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequences = TestUtils.create(3, 4, 3)
    val xs = run(() => dao.findAll().toSeq)
    assertEquals(xs.size, 3)
    val expected = videoSequences.flatMap(_.getVideoReferences.asScala)
    val found = xs.flatMap(_.getVideoReferences.asScala)
    assertEquals(found.size, expected.size)
    dao.close()

  test("findByName"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.create(3, 3, 3).head
    val opt = run(() => dao.findByName(videoSequence.getName))
    assert(opt.isDefined)
    val entity = opt.get
    assertEquals(entity.getName, videoSequence.getName)
    dao.close()

  test("findByVideoUUID"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequence = TestUtils.create(3, 3, 3).head
    val video = videoSequence.getVideos.get(0)
    val opt = run(() => dao.findByVideoUUID(video.getUuid))
    assert(opt.isDefined)
    dao.close()

  test("findByTimestamp"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequences = TestUtils.create(3, 3, 3)
    val videoSequence = videoSequences.head
    val video = videoSequence.getVideos.get(0)
    val ts = video.getStart.plus(video.getDuration.dividedBy(2))
    val xs = run(() => dao.findByTimestamp(ts).toSeq)
    assertEquals(xs.size, 1)
    dao.close()

  test("findByNameAndTimestamp"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequences = TestUtils.create(3, 3, 3)
    val videoSequence = videoSequences.head
    val video = videoSequence.getVideos.get(0)
    val ts = video.getStart.plus(video.getDuration.dividedBy(2))
    val xs = run(() => dao.findByNameAndTimestamp(videoSequence.getName, ts).toSeq)
    assertEquals(xs.size, 1)
    dao.close()

  test("findByCameraIDAndTimestamp"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequences = TestUtils.create(3, 3, 3)
    val videoSequence = videoSequences.head
    val video = videoSequence.getVideos.get(0)
    val ts = video.getStart.plus(video.getDuration.dividedBy(2))
    val xs = run(() => dao.findByCameraIDAndTimestamp(videoSequence.getCameraID, ts).toSeq)
    assertEquals(xs.size, 1)
    dao.close()

  test("findAllNames"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()

    val videoSequences = TestUtils.create(8, 2, 1)
    val videoSequence = videoSequences.head
    val xs = run(() => dao.findAllNames().toSeq)
    assertEquals(xs.size, videoSequences.size)
    dao.close()

  test("findAllCameraIDs"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequences = TestUtils.create(8, 2, 1)
    val cameras = videoSequences.map(_.getCameraID).distinct
    val xs = run(() => dao.findAllCameraIDs())
    assertEquals(xs.size, cameras.size)
    dao.close()

  test("findAllNamesByCameraID"):
    given dao: VideoSequenceDAOImpl = daoFactory.newVideoSequenceDAO()
    val videoSequences = TestUtils.create(8, 2, 1)
    val name = videoSequences.head.getName
    val xs = run(() => dao.findAllNamesByCameraID(videoSequences.head.getCameraID).toSeq.distinct)
    assertEquals(xs.size, 1)
    dao.close()

