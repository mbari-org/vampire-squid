/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
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

import java.time.{Duration, Instant}
import java.util.UUID
import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.duration.{Duration => SDuration}
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mbari.vampiresquid.repository.jpa.entity.VideoSequenceEntity
import java.{util => ju}
import org.mbari.vampiresquid.repository.jpa.entity.VideoEntity
import scala.jdk.CollectionConverters._

/**
  * Created by brian on 5/12/16.
  */
class VideoDAOSpec extends AnyFlatSpec with Matchers {

  private[this] val daoFactory = DevelopmentTestDAOFactory

  private[this] val duration = Duration.ofMinutes(15)
  private[this] val timeout  = SDuration(2, TimeUnit.SECONDS)
  private[this] val now      = Instant.now()

  private[this] val videoSequence = new VideoSequenceEntity(
    "A VideoSequence",
    "Thundar",
    "A VideoSequence",
    ju.List.of(
      new VideoEntity("A", now.minus(duration), duration),
      new VideoEntity("B", now, duration),
      new VideoEntity("C", now.plus(duration), duration)
    )
  )

  private var videoSequenceUUID: UUID = _

  private[this] val dao = daoFactory.newVideoDAO()

  "VideoDAOImpl" should "create" in {
    // Executing create assigns the uuid and lastUpdated fields values in our mutable object
    Await.result(dao.runTransaction(d => d.create(videoSequence.getVideos.asScala.head)), timeout)
    dao.entityManager.detach(videoSequence)
    val v =
      Await.result(dao.runTransaction(d => d.findByName(videoSequence.getVideos.asScala.head.getName())), timeout)
    v shouldBe defined
    videoSequenceUUID = videoSequence.getUuid()
  }

  it should "findByVideoSequenceUUID" in {
    val v =
      Await.result(dao.runTransaction(d => d.findByVideoSequenceUUID(videoSequenceUUID)), timeout)
    v should have size (3)
  }

  it should "findAll" in {
    val v = Await.result(dao.runTransaction(d => d.findAll()), timeout)
    v should have size (3)
  }

  it should "findByTimestamp" in {
    val v = Await.result(
      dao.runTransaction(d =>
        d.findByTimestamp(now.plus(duration.dividedBy(4)), duration.dividedBy(2))
      ),
      timeout
    )
    v should have size (1)
  }

  it should "update" in {
    val videoName = videoSequence.getVideos.asScala.head.getName()
    val v         = Await.result(dao.runTransaction(d => d.findByName(videoName)), timeout)
    v shouldBe defined
    dao.entityManager.detach(v.get)
    v.get.setName("D")
    Await.result(dao.runTransaction(d => d.update(v.get)), timeout)
    val v3 = Await.result(dao.runTransaction(d => d.findByName("D")), timeout)
    v3 shouldBe defined
  }

  it should "delete" in {
    val videoName = videoSequence.getVideos.asScala.last.getName // Don't use head. We changed the value in the db
    val v         = Await.result(dao.runTransaction(d => d.findByName(videoName)), timeout)
    v shouldBe defined
    Await.result(dao.runTransaction(d => d.delete(v.get)), timeout)
    val v2 = Await.result(dao.runTransaction(d => d.findByName(videoName)), timeout)
    v2 shouldBe empty
  }

  it should "deleteByPrimaryKey" in {
    val primaryKey = videoSequence.getVideos.asScala.head.getUuid()
    val v          = Await.result(dao.runTransaction(d => d.findByUUID(primaryKey)), timeout)
    v shouldBe defined
    Await.result(dao.runTransaction(d => d.deleteByUUID(primaryKey)), timeout)
    val v2 = Await.result(dao.runTransaction(d => d.findByUUID(primaryKey)), timeout)
    v2 shouldBe empty
  }

  daoFactory.cleanup()

}
