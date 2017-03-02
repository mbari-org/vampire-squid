package org.mbari.vars.vam.api

import java.time.Instant
import java.util.Base64

import org.mbari.vars.vam.controllers.{VideoController, VideoReferenceController, VideoSequenceController}
import org.mbari.vars.vam.dao.jpa.{Video, VideoReference, VideoSequence}

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-08-12T15:24:00
 */
class VideoReferenceApiSpec extends WebApiStack {

  private[this] val videoSequenceV1Api = {
    val videoSequenceController = new VideoSequenceController(daoFactory)
    new VideoSequenceV1Api(videoSequenceController)
  }

  private[this] val videoV1Api = {
    val videoController = new VideoController(daoFactory)
    new VideoV1Api(videoController)
  }

  private[this] val videoReferenceV1Api = {
    val videoReferenceController = new VideoReferenceController(daoFactory)
    new VideoReferenceV1Api(videoReferenceController)
  }

  addServlet(videoSequenceV1Api, "/v1/videosequence")
  addServlet(videoV1Api, "/v1/video")
  addServlet(videoReferenceV1Api, "/v1/videoreference")

  protected override def afterAll(): Unit = {
    val dao = daoFactory.newVideoSequenceDAO()

    dao.runTransaction(d => {
      val all = dao.findAll()
      all.foreach(dao.delete)
    })
    dao.close()

    super.afterAll()
  }

  "VideoReferenceV1Api" should "return an empty JSON array when the database is empty" in {
    get("/v1/videoreference") {
      status should be(200)
      body should equal("[]")
    }
  }

  val startDate = Instant.now()
  var aVideoSequence: VideoSequence = _
  var aVideo: Video = _
  var aVideoReference: VideoReference = _
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
    post(
      "/v1/videoreference",
      "video_uuid" -> aVideo.uuid.toString,
      "uri" -> "http://www.mbari.org/some/video.mp4",
      "container" -> "video/mp4",
      "video_codec" -> "h.264",
      "width" -> "1920",
      "height" -> "1080",
      "frame_rate" -> "30",
      "sha512" -> Base64.getEncoder.encodeToString(Array.fill[Byte](64)(9))) {
        status should be(200)
        aVideoReference = gson.fromJson(body, classOf[VideoReference])
      }
  }

  it should "get by uuid" in {
    get("/v1/videoreference/" + aVideoReference.uuid) {
      status should be(200)
      val videoReference = gson.fromJson(body, classOf[VideoReference])
      videoReference.uuid should be(aVideoReference.uuid)
    }
  }

  /* it should "get by uri" in {
    get("/v1/videoreference/uri/" + aVideoReference.uri) {
      status should be (200)
    }
  } */

  it should "update" in {
    put(
      "/v1/videoreference/" + aVideoReference.uuid,
      "size_bytes" -> "1234567") {
        status should be(200)
        val videoReference = gson.fromJson(body, classOf[VideoReference])
        videoReference.size should be(1234567)
      }
  }

  it should "delete" in {
    delete("/v1/videoreference/" + aVideoReference.uuid) {
      status should be(204)
    }
    get("/v1/videoreference/" + aVideoReference.uuid) {
      status should be(404)
    }
  }

}
