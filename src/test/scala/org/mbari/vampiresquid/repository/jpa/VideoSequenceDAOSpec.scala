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
import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration as SDuration
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mbari.vampiresquid.repository.jpa.entity.VideoSequenceEntity
import org.mbari.vampiresquid.repository.jpa.entity.VideoEntity
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity

import scala.jdk.CollectionConverters.*
import org.mbari.vampiresquid.domain.extensions.primaryKey

import scala.util.Random

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-06T15:43:00
  */
class VideoSequenceDAOSpec extends AnyFlatSpec with Matchers:

  private val daoFactory = TestDAOFactory.Instance

  private val timeout = SDuration(2, TimeUnit.SECONDS)

  private val dao = daoFactory.newVideoSequenceDAO()

  private val random = new Random()

  "VideoSequenceDAOImpl" should "create" in:

    val vs = new VideoSequenceEntity("T12345", "Tiburon")
    Await.result(dao.runTransaction(d => d.create(vs)), timeout)

    val vs2 = dao.findByName(vs.getName())
    vs2 shouldBe defined


  var videoSequence: VideoSequenceEntity = _
  it should "create w/ child objects" in:
    var vs = new VideoSequenceEntity("V05879", "Ventana")
    val d0 = Instant.parse("2016-04-01T00:15:00Z")
    vs.addVideo(new VideoEntity("V20160401T001500", d0, Duration.ofMinutes(15)))
    val d1  = Instant.parse("2016-04-01T00:30:00Z")
    val v1  = new VideoEntity("V20160401T003000", d1, Duration.ofMinutes(30))
    val vr1 = new VideoReferenceEntity(new URI("http://www.mbari.org/movies/test.mp4"))
    v1.addVideoReference(vr1)
    vs.addVideo(v1)

    Await.result(dao.runTransaction(d => d.create(vs)), timeout)
    val vs2 = dao.findByName(vs.getName())
    vs2 shouldBe defined
    val vs3 = vs2.get
    vs3.getVideos.size() should be(2)
    videoSequence = vs3

    // causes stack overflow on derby
  // it should "fail to create VideoSequence with a name already stored in the database" in:
  //   val vs = new VideoSequenceEntity(videoSequence.getName(), "Fubar")
  //   a[Exception] should be thrownBy:
  //     Await.result(dao.runTransaction(d => d.create(vs)), timeout)


  it should "update" in:
    val newCameraID = "Ventana"
    val updatedSequence = Await.result(dao.runTransaction(d => {
      val vs2 = d.findByName(videoSequence.getName())
      vs2 shouldBe defined
      vs2.get.setCameraID(newCameraID)
      vs2
    }), timeout) // Changes made in transactions should be propagated

    updatedSequence shouldBe defined
    updatedSequence.get.getName should be(videoSequence.getName())
    updatedSequence.get.getCameraID should be(newCameraID)

    val vs3 = Await.result(dao.runTransaction(d => d.findByName(videoSequence.getName())), timeout)
    vs3 shouldBe defined
    dao.entityManager.detach(vs3.get)
    vs3.get.getCameraID should be(newCameraID)

    vs3.get.setCameraID("Should not be persisted")

    val vs4 = dao.findByName(videoSequence.getName())
    vs4 shouldBe defined
    vs4.get.getCameraID should be(newCameraID)
    videoSequence = vs4.get

  it should "insert child videos in the datastore" in:
    val name          = "i2map 2009.123.04"
    val videoSequence = new VideoSequenceEntity(name, "i2map")
    videoSequence.addVideo(new VideoEntity("woah there nelly", Instant.now(), Duration.ofMinutes(15)))
    videoSequence.addVideo(new VideoEntity("woah there nelly II", Instant.now(), Duration.ofMinutes(30)))
    Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)

    val vs = dao.findByName(name)
    vs shouldBe defined
    vs.get.getVideos.size should be(2)

    val v = new VideoEntity("another one", Instant.now())
    vs.get.addVideo(v)
    Await.result(
      dao.runTransaction(d => d.update(vs.get)),
      timeout
    ) // update should insert our new video

    val vs2 = dao.findByName(name)
    vs2 shouldBe defined
    vs2.get.getVideos.size should be(3)


  var videoUUID: UUID = _
  it should "findByCameraID" in:
    val vs =
      Await.result(dao.runTransaction(d => d.findByCameraID(videoSequence.getCameraID())), timeout)
    vs should have size 1
    val a = vs.head
    a.getCameraID should be(videoSequence.getCameraID())

    // Setup for later test. We need a videoUUID and UUID
    videoUUID = a.getVideos.asScala.head.primaryKey.get


  it should "findByName" in:
    val vs = Await.result(dao.runTransaction(d => d.findByName(videoSequence.getName())), timeout)
    vs shouldBe defined
    val a = vs.get
    a.getName should be(videoSequence.getName())

  it should "findByVideoUUID" in:
    val vs = Await.result(
      dao.runTransaction(d => d.findByVideoUUID(videoSequence.getVideos.asScala.head.getUuid())),
      timeout
    )
    vs shouldBe defined
    val a = vs.get
    println(s"!!!!! ${a.getVideos.size}")
    val v = a.getVideos.asScala.filter(_.getUuid.equals(videoUUID))
    v should have size 1
    v.head.primaryKey.get should be(videoUUID)

  it should "findByTimestamp" in:
    val timestamp = videoSequence.getVideos.asScala.head.getStart()
    val duration  = videoSequence.getVideos.asScala.head.getDuration()
    val vs = Await.result(
      dao.runTransaction(d =>
        d.findByTimestamp(timestamp.plusMillis(duration.dividedBy(2).toMillis), duration)
      ),
      timeout
    )
    vs should have size (1)

  it should "findByNameAndTimestamp" in:
    val timestamp = videoSequence.getVideos.asScala.head.getStart()
    val duration  = videoSequence.getVideos.asScala.head.getDuration()
    val vs = Await.result(
      dao.runTransaction(d =>
        d.findByNameAndTimestamp(
          videoSequence.getName(),
          timestamp.plusMillis(duration.dividedBy(2).toMillis),
          duration
        )
      ),
      timeout
    )
    vs should have size (1)

  it should "delete" in:
    val vs = dao.findByName(videoSequence.getName())
    vs shouldBe defined
    Await.result(dao.runTransaction(d => d.delete(vs.get)), timeout)
    val vs1 = Await.result(dao.runTransaction(d => d.findByName(videoSequence.getName())), timeout)
    vs1 shouldBe empty

  it should "deleteByUUID" in:
    val name          = s"Brian's awesome AUV - ${random.nextLong(1000000)}"
    val videoSequence = new VideoSequenceEntity(name, "awesome")
    videoSequence.addVideo(new VideoEntity(s"woah there nelly - ${random.nextLong(1000000)}", Instant.now(), Duration.ofMinutes(15)))

    Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)

    val vs = dao.findByName(name)
    vs shouldBe defined

    Await.result(dao.runTransaction(d => d.deleteByUUID(vs.get.primaryKey.get)), timeout)

    val vs2 = dao.findByName(name)
    vs2 shouldBe empty


  it should "delete all" in:
    val all = Await.result(dao.runTransaction(d => d.findAll()), timeout)
    Await.result(dao.runTransaction(d => {
      all.foreach(d.delete)
    }), timeout)
    val allGone = Await.result(dao.runTransaction(d => d.findAll()), timeout)
    allGone.size should be(0)

  daoFactory.cleanup()

