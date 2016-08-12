package org.mbari.vars.vam.api

import java.time.Instant
import java.util

import org.mbari.vars.vam.controllers.{ VideoController, VideoSequenceController }
import org.mbari.vars.vam.dao.jpa.{ Video, VideoSequence }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-08-11T17:05:00
 */
class VideoApiSpec extends WebApiStack {

  private[this] val videoSequenceV1Api = {
    val videoSequenceController = new VideoSequenceController(daoFactory)
    new VideoSequenceV1Api(videoSequenceController)
  }

  private[this] val videoV1Api = {
    val videoController = new VideoController(daoFactory)
    new VideoV1Api(videoController)
  }

  addServlet(videoSequenceV1Api, "/v1/videosequence")
  addServlet(videoV1Api, "/v1/video")

  protected override def afterAll(): Unit = {
    val dao = daoFactory.newVideoSequenceDAO()

    dao.runTransaction(d => {
      val all = dao.findAll()
      all.foreach(dao.delete)
    })
    dao.close()

    super.afterAll()
  }

  "VideoV1Api" should "return an empty JSON array when the database is empty" in {
    get("/v1/video") {
      status should be(200)
      body should equal("[]")
    }
  }

  val startDate = Instant.now()
  var aVideoSequence: VideoSequence = _
  var aVideo: Video = _
  it should "insert" in {
    post("/v1/videosequence", "name" -> "T1234", "camera_id" -> "Tiburon") {
      status should be(200)
      aVideoSequence = gson.fromJson(body, classOf[VideoSequence])
    }
    post(
      "/v1/video",
      "name" -> "T1234-01",
      "video_sequence_uuid" -> aVideoSequence.uuid.toString,
      "start" -> startDate.toString,
      "duration_millis" -> s"${15 * 60 * 1000}") {
        status should be(200)
        body should include("name")
        body should include("uuid")
        body should include("start")
        body should include("duration_millis")
        aVideo = gson.fromJson(body, classOf[Video])
      }
  }

  it should "get a videosequence by video UUID" in {
    get("/v1/video/videosequence/" + aVideo.uuid) {
      status should be(200)
      val videoSequence = gson.fromJson(body, classOf[VideoSequence])
      videoSequence should equal(aVideoSequence)
      videoSequence.videos.map(_.uuid) should contain(aVideo.uuid)
    }
  }

  it should "get by name" in {
    get("/v1/video/name/" + aVideo.name) {
      status should be(200)
      val video = gson.fromJson(body, classOf[Video])
      video.uuid should equal(aVideo.uuid)
      video.name should equal(aVideo.name)
    }
  }

  it should "get by timestamp" in {
    get("/v1/video/timestamp/" + startDate.plusMillis(1000)) {
      status should be(200)
      val video = gson.fromJson(body, classOf[util.ArrayList[Video]])
      video should not be empty
    }
  }

  it should "get between timestamps" in {
    get("/v1/video/timestamp/" + startDate.minusMillis(1000) + "/" + startDate.plusMillis(10000)) {
      status should be(200)
      val video = gson.fromJson(body, classOf[util.ArrayList[Video]])
      video should not be empty
    }
  }

  it should "update" in {
    put("/v1/video/" + aVideo.uuid, "description" -> "a description") {
      status should be(200)
      val video = gson.fromJson(body, classOf[Video])
      video.description should equal("a description")
    }
  }

  it should "delete" in {
    delete("/v1/video/" + aVideo.uuid) {
      status should be(204)
    }
    get("/v1/video/" + aVideo.uuid) {
      status should be(404)
    }
  }

}
