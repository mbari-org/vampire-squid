package org.mbari.vars.vam.controller

import org.mbari.vars.vam.controllers.VideoSequenceController
import org.scalactic.source.Position
import org.scalatest.{ BeforeAndAfter, BeforeAndAfterAll, FlatSpec, Matchers }

import scala.concurrent.Await

/**
 * @author Brian Schlining
 * @since 2017-04-05T14:28:00
 */
class VideoSequenceControllerSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  private[this] val n = 2
  private[this] val videoSequences = TestUtils.create(n, 10, 10)
  private[this] val controller = new VideoSequenceController(TestUtils.DaoFactory)
  import TestUtils.executionContext

  "VideoSequenceController" should "findAll" in {
    val fn = controller.findAll
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
  }

  it should "findAllNames" in {
    val fn = controller.findAllNames
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
  }

  it should "findByUUID" in {
    val fn = controller.findAll
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
    val uuid = rs.head.uuid
    val fn0 = controller.findByUUID(uuid)
    val rs0 = Await.result(fn0, TestUtils.Timeout)
    rs0 should not be (empty)
    rs0.get.uuid should be(uuid)
  }

  it should "findAllCameraIDs" in {
    val fn = controller.findAllCameraIDs
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
  }

  it should "findByName" in {
    val fn = controller.findAll
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
    val name = rs.head.name
    val fn0 = controller.findByName(name)
    val rs0 = Await.result(fn0, TestUtils.Timeout)
    rs0 should not be (empty)
    rs0.get.name should be(name)
  }

  it should "findByCameraIDAndTimestamp" in {
    val fn = controller.findAll
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
    val cameraId = rs.head.cameraID
    val timestamp = rs.head.videos.head.start
    val fn0 = controller.findByCameraIDAndTimestamp(cameraId, timestamp)
    val rs0 = Await.result(fn0, TestUtils.Timeout)
    rs0 should not be (empty)
  }

  override protected def afterAll(): Unit = TestUtils.DaoFactory.cleanup()
}
