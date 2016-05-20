package org.mbari.vars.vam.dao.jpa

import java.time.{ Duration, Instant }
import java.util.UUID
import java.util.concurrent.TimeUnit

import org.scalatest.{ FlatSpec, Matchers }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration => SDuration }
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by brian on 5/12/16.
 */
class VideoDAOSpec extends FlatSpec with Matchers {

  private[this] val duration = Duration.ofMinutes(15)
  private[this] val timeout = SDuration(2, TimeUnit.SECONDS)
  private[this] val now = Instant.now()

  private[this] val videoSequence = VideoSequence("A VideoSequence", "Thundar",
    Seq(
      Video("A", now.minus(duration), duration),
      Video("B", now, duration),
      Video("C", now.plus(duration), duration)))

  private var videoSequenceUUID: UUID = _

  private[this] val dao = H2TestDAOFactory.newVideoDAO()

  "VideoDAOImpl" should "create" in {
    // Executing create assigns the uuid and lastUpdated fields values in our mutable object
    Await.result(dao.runTransaction(d => d.create(videoSequence.videos.head)), timeout)
    dao.entityManager.detach(videoSequence)
    val v = Await.result(dao.runTransaction(d => d.findByName(videoSequence.videos.head.name)), timeout)
    v shouldBe defined
    videoSequenceUUID = videoSequence.uuid
  }

  it should "findByVideoSequenceUUID" in {
    val v = Await.result(dao.runTransaction(d => d.findByVideoSequenceUUID(videoSequenceUUID)), timeout)
    v should have size (3)
  }

  it should "findAll" in {
    val v = Await.result(dao.runTransaction(d => d.findAll()), timeout)
    v should have size (3)
  }

  it should "findByTimestamp" in {
    val v = Await.result(dao.runTransaction(d => d.findByTimestamp(now, duration.dividedBy(2))), timeout)
    v should have size (1)
  }

  it should "update" in {
    val videoName = videoSequence.videos.head.name
    val v = Await.result(dao.runTransaction(d => d.findByName(videoName)), timeout)
    v shouldBe defined
    dao.entityManager.detach(v.get)
    v.get.name = "D"
    Await.result(dao.runTransaction(d => d.update(v.get)), timeout)
    val v3 = Await.result(dao.runTransaction(d => d.findByName("D")), timeout)
    v3 shouldBe defined
  }

  it should "delete" in {
    val videoName = videoSequence.videos.last.name // Don't use head. We changed the value in the db
    val v = Await.result(dao.runTransaction(d => d.findByName(videoName)), timeout)
    v shouldBe defined
    Await.result(dao.runTransaction(d => d.delete(v.get)), timeout)
    val v2 = Await.result(dao.runTransaction(d => d.findByName(videoName)), timeout)
    v2 shouldBe empty
  }

  it should "deleteByPrimaryKey" in {
    val primaryKey = videoSequence.videos.head.uuid
    val v = Await.result(dao.runTransaction(d => d.findByPrimaryKey(primaryKey)), timeout)
    v shouldBe defined
    Await.result(dao.runTransaction(d => d.deleteByPrimaryKey(primaryKey)), timeout)
    val v2 = Await.result(dao.runTransaction(d => d.findByPrimaryKey(primaryKey)), timeout)
    v2 shouldBe empty
  }

}
