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
import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.duration.{Duration => SDuration}
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity
import scala.util.Random
import org.mbari.vampiresquid.repository.jpa.entity.VideoSequenceEntity
import java.{util => ju}
import org.mbari.vampiresquid.repository.jpa.entity.VideoEntity

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-18T14:04:00
  */
class VideoReferenceDAOSpec extends AnyFlatSpec with Matchers:

  private val daoFactory = TestDAOFactory.Instance

  private[this] val timeout = SDuration(2, TimeUnit.SECONDS)

  private[this] val dao = daoFactory.newVideoReferenceDAO()

  // --- Test setup
  val name0 = "T0123"
  val videoReference0 = new VideoReferenceEntity(
    new URI("http://foo.bar/somevideo.mp4"),
    "video/mp4",
    "hevc",
    "pcm_s24le",
    1920,
    1080,
    29.97,
    Random.between(10000, 100000),
    "some description",
    Array.fill[Byte](64)(10)
  )

  val videoSequence0 = new VideoSequenceEntity(
    name0,
    "Bar",
    "a description",
    ju.List.of(
      new VideoEntity(
        "bar1",
        Instant.now,
        Duration.ofMillis(Random.between(1000, 100000)),
        ju.List.of(new VideoReferenceEntity(new URI("uri:mbari:tape:T0123-04HD")), videoReference0)
      ),
      new VideoEntity("bar2", Instant.now, Duration.ofSeconds(23))
    )
  )

  "VideoReferenceDAOImpl" should "create a record in the datastore" in:
    Await.result(dao.runTransaction(d => d.create(videoReference0)), timeout)
    val videoReference2 = dao.findByURI(videoReference0.getUri())
    videoReference2 shouldBe defined
    videoReference2.get.getSha512 should be(Array.fill[Byte](64)(10))

  it should "update a record in the datastore" in:
    Await.result(dao.runTransaction(d => {
      val vr = dao.findByURI(videoReference0.getUri())
      vr.foreach(v => v.setContainer("foo"))
    }), timeout)

    val vr = dao.findByURI(videoReference0.getUri())
    vr shouldBe defined
    vr.get.getContainer should be("foo")

  // it should "delete a record in the datastore" in:
  //   val vr = dao.findByURI(videoReference0.getUri())
  //   vr shouldBe defined
  //   Await.result(dao.runTransaction(d => d.delete(vr.get)), timeout)
  //   dao.findByURI(videoReference0.getUri()) shouldBe empty

  it should "throw an exception if no parent video is assigned" in:
    val vr = new VideoReferenceEntity(
      new URI("http://foo.bar/someothervideo.mp4"),
      "video/mp4",
      "hevc",
      "pcm_s24le",
      1920,
      1080,
      30,
      Random.between(10000, 100000),
      "some description",
    )

    a[Exception] should be thrownBy:
      Await.result(dao.runTransaction(d => d.create(vr)), timeout)

  // --- Test  setup for multiple refs
  val name1 = "T9999"
  val videoReference1 = new VideoReferenceEntity(
    new URI("http://foo.bar/somevideoagain.mp4"),
    "video/mp4",
    "hevc",
    "pcm_s24le",
    1920,
    1080,
    30,
      Random.between(10000, 100000),
      "some description",
  )

  val video1 = new VideoEntity(
    "bar11",
    Instant.now,
    ju.List.of(new VideoReferenceEntity(new URI("uri:mbari:tape:T9999-08HD")), videoReference1)
  )

  val videoSequence1 =
    new VideoSequenceEntity(name1, "Bar", "", ju.List.of(video1, new VideoEntity("bar22", Instant.now, Duration.ofSeconds(23))))

  it should "create and findByUUID" in:
    Await.result(dao.runTransaction(d => d.create(videoReference1)), timeout)
    val v = Await.result(dao.runTransaction(d => d.findByVideoUUID(video1.getUuid())), timeout)
    v.size should be(video1.getVideoReferences.size)

  it should "findAll" in:
    val vs = Await.result(dao.runTransaction(d => d.findAll()), timeout)
    vs.size should be >= video1.getVideoReferences.size

  it should "findAllURIs" in:
    val uris = Await.result(dao.runTransaction(d => d.findAllURIs()), timeout)
    uris.size should be >= video1.getVideoReferences.size

  it should "findByVideoUUID" in:
    val vs = Await.result(dao.runTransaction(d => d.findByVideoUUID(video1.getUuid())), timeout)
    vs.size should be >= video1.getVideoReferences.size

  it should "findByURI" in:
    val vr = Await.result(dao.runTransaction(d => d.findByURI(videoReference1.getUri())), timeout)
    vr should not be (empty)

  // it should "deleteByPrimaryKey" in:
  //   Await.result(dao.runTransaction(d => d.deleteByUUID(videoReference1.getUuid())), timeout)
  //   val vr = Await.result(dao.runTransaction(d => d.findByUUID(videoReference1.getUuid())), timeout)
  //   vr shouldBe empty

  daoFactory.cleanup()

