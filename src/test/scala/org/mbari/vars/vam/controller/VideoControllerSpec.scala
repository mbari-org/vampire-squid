package org.mbari.vars.vam.controller

import java.time.{ Duration, Instant }
import java.util.concurrent.TimeUnit

import org.mbari.vars.vam.controllers.{ VideoController, VideoSequenceController }
import org.mbari.vars.vam.dao.jpa.{ DerbyTestDAOFactory, H2TestDAOFactory, Video, VideoSequence }
import org.scalatest.{ BeforeAndAfterEach, FlatSpec, Matchers }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration => SDuration }
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-08-12T10:56:00
 */
class VideoControllerSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  private[this] val controller = new VideoController(H2TestDAOFactory)
  private[this] val vsController = new VideoSequenceController(H2TestDAOFactory)

  private[this] val duration = Duration.ofMinutes(15)
  private[this] val timeout = SDuration(2, TimeUnit.SECONDS)
  private[this] val now = Instant.now()

  "VideoController" should "create" in {
    val fn = vsController.create("A VideoSequence", "Thundar").map(vs => {
      controller.create(vs.uuid, "Proxy", now, Some(duration))
    })
    val video = Await.result(fn, timeout)
    video should not be (null)
  }

  it should "find by timestamp" in {
    val videos = Await.result(controller.findByTimestamp(now.plusMillis(1000)), timeout)
    videos.size should be(1)
  }

}
