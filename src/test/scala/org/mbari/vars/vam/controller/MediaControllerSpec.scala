package org.mbari.vars.vam.controller

import java.net.URI
import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit

import org.mbari.vars.vam.controllers.{MediaController, VideoReferenceController, VideoSequenceController}
import org.mbari.vars.vam.dao.jpa.DevelopmentTestDAOFactory
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.{Duration => SDuration}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * @author Brian Schlining
 * @since 2017-03-06T15:54:00
 */
class MediaControllerSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  private[this] val daoFactory = DevelopmentTestDAOFactory
  private[this] val controller = new MediaController(daoFactory)
  private[this] val vsController = new VideoSequenceController(daoFactory)
  private[this] val vrController = new VideoReferenceController(daoFactory)
  private[this] val timeout = SDuration(2, TimeUnit.SECONDS)

  "MediaController" should "create with minimal arguments" in {

    val fn0 = controller.create(
      getClass.getSimpleName,
      "Ventana",
      "V20160711T012345",
      new URI("http://www.mbari.org/movies/airship.mp4"),
      Instant.parse("2016-07-11T01:23:45Z"))

    Await.result(fn0, timeout)

    val fn1 = vsController.findByName(getClass.getSimpleName)
    val vs = Await.result(fn1, timeout)
    vs shouldBe defined

  }

  it should "create when existing VideoSequence name is found" in {
    val fn0 = controller.create(
      getClass.getSimpleName,
      "Ventana",
      "V20160811T012345",
      new URI("http://www.mbari.org/movies/airship_proxy.mp4"),
      Instant.parse("2016-08-11T01:23:45Z"))

    Await.result(fn0, timeout)

    val fn1 = vsController.findByName(getClass.getSimpleName)
    val vs = Await.result(fn1, timeout)
    vs shouldBe defined
    val v = vs.get
    v.videos.size should be(2)
  }

  it should "create when existing video name is found" in {
    val fn0 = controller.create(
      getClass.getSimpleName,
      "Ventana",
      "V20160811T012345",
      new URI("http://www.mbari.org/movies/airship_mezzanine.mp4"),
      Instant.parse("2016-08-11T01:23:45Z"))

    Await.result(fn0, timeout)

    val fn1 = vsController.findByName(getClass.getSimpleName)
    val vs = Await.result(fn1, timeout)
    vs shouldBe defined
    val v = vs.get
    //println(controller.toJson(v))
    v.videoReferences.size should be(3)
  }

  it should "create with all params" in {
    val fn0 = controller.create(
      getClass.getSimpleName,
      "Ventana",
      "V20160911T012345",
      new URI("http://www.mbari.org/movies/airship_another.mp4"),
      Instant.parse("2016-08-11T01:23:45Z"),
      Some(Duration.ofMinutes(25)),
      Some("video/mp4"),
      Some("h264"),
      Some("aac"),
      Some(1920),
      Some(1080),
      Some(30),
      Some(12345678),
      Some("A test movie"),
      Some(Array.ofDim[Byte](64)))
    Await.result(fn0, timeout)

    val fn1 = vsController.findByName(getClass.getSimpleName)
    val vs = Await.result(fn1, timeout)
    vs shouldBe defined
    val v = vs.get
    //println(controller.toJson(v))
    v.videoReferences.size should be(4)
  }

}
