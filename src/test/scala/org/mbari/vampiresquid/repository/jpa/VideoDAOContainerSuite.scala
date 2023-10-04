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

import scala.jdk.CollectionConverters.*

trait VideoDAOContainerSuite extends BaseDAOSuite:

  override def beforeAll(): Unit = daoFactory.beforeAll()
  override def afterAll(): Unit  = daoFactory.afterAll()

  test("create"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.build(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    run(() => dao.create(video))
    val videoEntity = run(() => dao.findByUUID(video.getUuid))
    assert(videoEntity.isDefined)
    dao.close()
  
  test("update"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    val name = video.getName
    val newName = "foobarbaz"
    video.setName(newName)
    run(() => dao.update(video))
    val opt = run(() => dao.findByUUID(video.getUuid))
    assert(opt.isDefined)
    val entity = opt.get
    assertEquals(entity.getName, newName)
    dao.close()

  test("update cascades to videoReferences"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    val videoReference = video.getVideoReferences.get(0)
    val newAudioCodec = "audio/aaaaaaac"
    videoReference.setAudioCodec(newAudioCodec)
    run(() => dao.update(video))
    val opt = run(() => dao.findByUUID(video.getUuid))
    assert(opt.isDefined)
    val entity = opt.get.getVideoReferences.get(0)
    assertEquals(entity.getAudioCodec, newAudioCodec)
    dao.close()
  
  test("delete"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    run(() => {
      val entity = dao.update(video) // Have to merge into persistent context before deleting
      dao.delete(entity)
    })
    val opt = run(() => dao.findByUUID(video.getUuid))
    assert(opt.isEmpty)
    dao.close()
  
  test("deleteByUUID"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    run(() => dao.deleteByUUID(video.getUuid))
    val opt = run(() => dao.findByUUID(video.getUuid))
    assert(opt.isEmpty)
    dao.close()
  
  test("findByUUID"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    val opt = run(() => dao.findByUUID(video.getUuid))
    assert(opt.isDefined)
    dao.close()
  
  test("findAll"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(3, 3, 1).head
    val xs = run(() => dao.findAll().toSeq)
    assertEquals(xs.size, 9)
    dao.close()

  test("findByName"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    val opt = run(() => dao.findByName(video.getName))
    assert(opt.isDefined)
    val entity = opt.get
    assertEquals(entity.getName, video.getName)
    dao.close()

  test("findByVideoSequenceUUID"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 3, 1).head
    val xs = run(() => dao.findByVideoSequenceUUID(videoSequence.getUuid).toSeq)
    assertEquals(xs.size, 3)
    dao.close()

  test("findByVideoReferenceUUID"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 3).head
    val videoReference = videoSequence.getVideos.get(0).getVideoReferences.get(0)
    val opt = run(() => dao.findByVideoReferenceUUID(videoReference.getUuid))
    assert(opt.isDefined)
    dao.close()


  test("findByTimestamp"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    // find by start
    val xs = run(() => dao.findByTimestamp(video.getStart).toSeq)
    assertEquals(xs.size, 1)
    // find by time in middle of video
    val t = video.getStart.plus(video.getDuration.dividedBy(2))
    val ys = run(() => dao.findByTimestamp(t).toSeq)
    assertEquals(ys.size, 1)
    dao.close()


  test("findBetweenTimestamps"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(1, 1, 1).head
    val video = videoSequence.getVideos.get(0)
    val t0 = video.getStart.minus(video.getDuration.dividedBy(2))
    val t1 = video.getStart.plus(video.getDuration.dividedBy(2))

    val xs = run(() => dao.findBetweenTimestamps(t0, t1).toSeq)
    assertEquals(xs.size, 1)

    val t2 = video.getStart.minus(video.getDuration.dividedBy(3))
    val ys = run(() => dao.findBetweenTimestamps(t0, t2).toSeq)
    assertEquals(ys.size, 0)
    dao.close()

  test("findAllNames"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(3, 3, 1)
    val names = videoSequence.flatMap(_.getVideos.asScala)
      .map(_.getName)
      .sorted
    val entityNames = run(() => dao.findAllNames().toSeq.sorted)
    assertEquals(entityNames, names)
    dao.close()

  test("findAllNamesAndTimestamps"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequence = TestUtils.create(3, 3, 1)
    val xs = videoSequence.flatMap(_.getVideos.asScala)
      .map(v => (v.getName, v.getStart))
      .sortBy(_._1)
      .toList
    val ys = run(() => dao.findAllNamesAndTimestamps().toSeq.sortBy(_._1)).toList
    assertEquals(ys.map(_._1).sorted, xs.map(_._1).sorted)
    assertEquals(ys.map(_._2).sorted, xs.map(_._2).sorted)
    dao.close()

  test("findNameByVideoSequenceName"):
    given dao: VideoDAOImpl = daoFactory.newVideoDAO()
    val videoSequences = TestUtils.create(3, 3, 1)
    val videoSequence = videoSequences.head
    val xs = run(() => dao.findNamesByVideoSequenceName(videoSequence.getName).toSeq.sorted)
    assertEquals(xs.size, 3)
    val names = videoSequence.getVideos.asScala.map(_.getName).sorted.toSeq
    assertEquals(xs, names)

class DerbyVideoDAOSuite extends VideoDAOContainerSuite:
  override val daoFactory = DerbyTestDAOFactory

class PostgresVideoDAOSuite extends VideoDAOContainerSuite:
  override def daoFactory = PostgresqlDAOFactory

class OracleVideoDAOSuite extends VideoDAOContainerSuite:
  override def daoFactory = OracleDAOFactory
  
  

