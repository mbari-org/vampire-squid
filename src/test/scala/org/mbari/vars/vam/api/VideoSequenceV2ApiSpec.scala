package org.mbari.vars.vam.api

import java.nio.charset.StandardCharsets

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.controller.TestUtils
import org.mbari.vars.vam.controllers.{VideoReferenceController, VideoSequenceController}
import org.mbari.vars.vam.dao.jpa.VideoSequence
import org.slf4j.LoggerFactory

/**
  * @author Brian Schlining
  * @since 2017-04-18T13:50:00
  */
class VideoSequenceV2ApiSpec extends WebApiStack {

  private[this] val videoSequenceV2Api = new VideoSequenceV2Api(new VideoSequenceController(daoFactory))
  private[this] val name = getClass.getSimpleName
  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val timeout = TestUtils.Timeout

  addServlet(videoSequenceV2Api, "/v2/videosequences")

  private def makeName(i: Int) = s"${getClass.getSimpleName}_$i"

  "VideoSequenceV2ApiSpec" should "create via post (form body)" in {
    val videoSequence = TestUtils.createVideoSequence(makeName(1), makeName(1) + "A")
    val b = s"name=${videoSequence.name}&camera_id=${videoSequence.cameraID}"
    post("v2/videosequences",
        body = b.getBytes(StandardCharsets.UTF_8),
        headers = Seq("Content-Type" -> "application/form")) {

      status should be (200)
      val vs = gson.fromJson(body, classOf[VideoSequence])
      vs.name should be (videoSequence.name)
      vs.cameraID should be (videoSequence.cameraID)
      vs.uuid should not be (null)
    }
  }

  it should "create via post (json body)" in {
    val videoSequence = TestUtils.createVideoSequence(makeName(2), makeName(2) + "A")
    val b = Constants.GSON.toJson(videoSequence)
    post("v2/videosequences",
        body = b.getBytes(StandardCharsets.UTF_8),
        headers = Seq("Content-Type" -> "application/json")) {

      status should be (200)
      val vs = gson.fromJson(body, classOf[VideoSequence])
      vs.name should be (videoSequence.name)
      vs.cameraID should be (videoSequence.cameraID)
      vs.uuid should not be (null)
    }
  }

}
