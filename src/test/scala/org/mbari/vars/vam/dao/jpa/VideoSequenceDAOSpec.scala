package org.mbari.vars.vam.dao.jpa

import java.time.{ Duration, Instant }
import java.util.UUID
import java.util.concurrent.TimeUnit

import org.scalatest.{ FlatSpec, Matchers }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration => SDuration }
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-06T15:43:00
 */
class VideoSequenceDAOSpec extends FlatSpec with Matchers {

  private[this] val timeout = SDuration(2, TimeUnit.SECONDS)

  private[this] val dao = H2TestDAOFactory.newVideoSequenceDAO()

  val name0 = "T012345"
  val videoSequence0 = VideoSequence(name0, "Tiburon")

  "VideoSequenceDAOImpl" should "create" in {

    Await.result(dao.runTransaction(d => d.create(videoSequence0)), timeout)

    val videoSequence2 = dao.findByName(name0)
    videoSequence2 shouldBe defined
  }

  it should "update" in {
    val newCameraID = "Ventana"
    val updatedSequence = Await.result(dao.runTransaction(d => {
      val vs2 = d.findByName(name0)
      vs2 shouldBe defined
      vs2.get.cameraID = newCameraID
      vs2
    }), timeout) // Changes made in transactions should be propagated

    updatedSequence shouldBe defined
    updatedSequence.get.name should be(videoSequence0.name)
    updatedSequence.get.cameraID should be(newCameraID)

    val vs3 = Await.result(dao.runTransaction(d => d.findByName(name0)), timeout)
    vs3 shouldBe defined
    dao.entityManager.detach(vs3.get)
    vs3.get.cameraID should be(newCameraID)

    vs3.get.cameraID = "Should not be persisted"

    val vs4 = dao.findByName(name0)
    vs4 shouldBe defined
    vs4.get.cameraID should be(newCameraID)
  }

  it should "delete" in {
    val vs = dao.findByName(name0)
    vs shouldBe defined
    Await.result(dao.runTransaction(d => d.delete(vs.get)), timeout)
    val vs1 = Await.result(dao.runTransaction(d => d.findByName(name0)), timeout)
    vs1 shouldBe empty
  }

  it should "insert child videos in the datastore" in {
    val name = "i2map 2009.123.04"
    val videoSequence = VideoSequence(name, "i2map")
    videoSequence.addVideo(Video("woah there nelly", Instant.now()))
    videoSequence.addVideo(Video("woah there nelly II", Instant.now()))
    Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)

    val vs = dao.findByName(name)
    vs shouldBe defined
    vs.get.videos.size should be(2)

    val v = Video("another one", Instant.now())
    vs.get.addVideo(v)
    Await.result(dao.runTransaction(d => d.update(vs.get)), timeout) // update should insert our new video

    val vs2 = dao.findByName(name)
    vs2 shouldBe defined
    vs2.get.videos.size should be(3)

    Await.result(dao.runTransaction(d => d.delete(vs2.get)), timeout)

  }

  it should "delete using primary key" in {
    val name = "Brian's awesome AUV - 123456789"
    val videoSequence = VideoSequence(name, "awesome", Seq(Video("foo", Instant.now())))
    Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)

    val vs = dao.findByName(name)
    vs shouldBe defined

    Await.result(dao.runTransaction(d => d.deleteByPrimaryKey(vs.get.primaryKey.get)), timeout)

    val vs2 = dao.findByName(name)
    vs2 shouldBe empty

  }

  val name = "Foo"
  val cameraID = "BarBarBar"
  val timestamp = Instant.now()
  val duration = Duration.ofMinutes(15)
  val videoSequence = VideoSequence(name, cameraID,
    Seq(
      Video("foo1", timestamp.minus(Duration.ofHours(10)), duration),
      Video("foo2", timestamp, duration)))
  Await.result(dao.runTransaction(d => d.create(videoSequence)), timeout)
  var uuid: UUID = _
  var videoUUID: UUID = _

  it should "findByCameraID" in {
    val vs = Await.result(dao.runTransaction(d => d.findByCameraID(cameraID)), timeout)
    vs should have size 1
    val a = vs.head
    a.cameraID should be(cameraID)

    // Setup for later test. We need a videoUUID and UUID
    uuid = a.primaryKey.get
    videoUUID = a.videos.head.primaryKey.get

  }

  it should "findByName" in {
    val vs = Await.result(dao.runTransaction(d => d.findByName(name)), timeout)
    vs shouldBe defined
    val a = vs.get
    a.name should be(name)
  }

  it should "findByVideoUUID" in {
    val vs = Await.result(dao.runTransaction(d => d.findByVideoUUID(videoUUID)), timeout)
    vs shouldBe defined
    val a = vs.get
    val v = a.videos.filter(_.uuid.equals(videoUUID))
    v should have size 1
    v.head.primaryKey.get should be(videoUUID)
  }

  it should "findByTimestamp" in {
    val vs = Await.result(dao.runTransaction(d => d.findByTimestamp(timestamp.plusSeconds(600))), timeout)
    vs should have size (1)
  }

  it should "findByNameAndTimestamp" in {
    val vs = Await.result(dao.runTransaction(d => d.findByNameAndTimestamp(name, timestamp.plusSeconds(600))), timeout)
    vs should have size (1)
  }

  it should "deleteByPrimaryKey" in {
    Await.result(dao.runTransaction(d => d.deleteByPrimaryKey(uuid)), timeout)
    val vs = Await.result(dao.runTransaction(d => d.findByName(name)), timeout)
    vs shouldBe empty
  }

  it should "delete all" in {
    val all = Await.result(dao.runTransaction(d => d.findAll()), timeout)
    Await.result(dao.runTransaction(d => {
      all.foreach(d.delete)
    }), timeout)
    val allGone = Await.result(dao.runTransaction(d => d.findAll()), timeout)
    allGone.size should be(0)
  }

}
