package org.mbari.vars.vam.dao.jpa


import java.time.Instant
import java.util.concurrent.TimeUnit

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-06T15:43:00
  */
class VideoSequenceDAOSpec extends FlatSpec with Matchers {

  private[this] val timeout = Duration(2, TimeUnit.SECONDS)

  "TestDAOFactory" should "create a VideoSequenceDAO" in {
    val dao = TestDAOFactory.newVideoSequenceDAO()
    dao should not be null
  }

  "VideoSequenceDAOImpl" should "create a record in the datastore" in {
    val name = "T01234"
    val dao = TestDAOFactory.newVideoSequenceDAO()

    val videoSequence = VideoSequence(name, "Tiburon")
    Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)

    val videoSequence2 = dao.findByName(name)
    videoSequence2 shouldBe defined
  }

  it should "create, update and delete a record in the datastore" in {
    val name = "T012345"
    val dao = TestDAOFactory.newVideoSequenceDAO()

    val videoSequence = VideoSequence(name, "Tiburon")
    Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)

    val updatedSequence = Await.result(dao.runTransaction(d => {
      val vs2 = d.findByName(name)
      vs2 shouldBe defined
      vs2.get.cameraID = "Ventana"
      vs2
    }), timeout) // Changes made in transactions should be propagated

    updatedSequence shouldBe defined
    updatedSequence.get.name === "Ventana"

    val vs3 = Await.result(dao.runTransaction(d => d.findByName(name)), timeout)
    vs3 shouldBe defined
    dao.entityManager.detach(vs3.get)
    vs3.get.cameraID should be ("Ventana")

    vs3.get.cameraID = "Should not be persisted"

    val vs4 = dao.findByName(name)
    vs4 shouldBe defined
    vs4.get.cameraID should be ("Ventana")

    Await.result(dao.runTransaction(d => d.delete(vs4.get)), timeout)
    val vs5 = Await.result(dao.runTransaction(d => d.findByName(name)), timeout)
    vs5 shouldBe empty

  }

  it should "insert child videos in the datastore" in {
    val name = "i2map 2009.123.04"
    val dao = TestDAOFactory.newVideoSequenceDAO()
    val videoSequence = VideoSequence(name, "i2map")
    videoSequence.addVideo(Video("woah there nelly", Instant.now()))
    videoSequence.addVideo(Video("woah there nelly II", Instant.now()))
    Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)

    val vs = dao.findByName(name)
    vs shouldBe defined
    vs.get.videos.size should be (2)

    val v = Video("another one", Instant.now())
    vs.get.addVideo(v)
    Await.result(dao.runTransaction(d => d.update(vs.get)), timeout) // update should insert our new video

    val vs2 = dao.findByName(name)
    vs2 shouldBe defined
    vs2.get.videos.size should be (3)

    Await.result(dao.runTransaction(d => d.delete(vs2.get)), timeout)

  }

  it should "delete using primary key" in {
    val name = "Brian's awesome AUV - 123456789"
    val dao = TestDAOFactory.newVideoSequenceDAO()
    val videoSequence = VideoSequence(name, "awesome", Seq(Video("foo", Instant.now())))
    Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)

    val vs = dao.findByName(name)
    vs shouldBe defined

    Await.result(dao.runTransaction(d => d.deleteByPrimaryKey(vs.get.primaryKey.get)), timeout)

    val vs2 = dao.findByName(name)
    vs2 shouldBe empty

  }



}
