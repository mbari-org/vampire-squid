package org.mbari.vars.vam.api

import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.dao.jpa.ByteArrayConverter
import org.mbari.vars.vam.model.Media

/**
 * @author Brian Schlining
 * @since 2017-03-06T17:20:00
 */
class MediaV1ApiSpec extends WebApiStack {

  private[this] val mediaV1Api = new MediaV1Api(new MediaController(daoFactory))
  private[this] val name = getClass.getSimpleName

  addServlet(mediaV1Api, "/v1/media")

  "MediaV1Api" should "create w/ minimal args" in {
    post("v1/media", "video_sequence_name" -> name,
      "camera_id" -> "Ventana",
      "video_name" -> "V19680922T030001Z",
      "uri" -> "http://www.mbari.org/movies/somemovie.mov",
      "start_timestamp" -> "1968-09-22T03:00:01Z") {
        status should be(200)
        val media = gson.fromJson(body, classOf[Media])
        media.videoSequenceUuid should not be (null)
        media.videoUuid should not be (null)
        media.videoReferenceUuid should not be (null)
      }
  }

  it should "create w/ all args" in {

    val sha512 = ByteArrayConverter.encode(Array.fill[Byte](64)(11))
    sha512.size should be(128)

    post("/v1/media", "video_sequence_name" -> s"$name-bob",
      "camera_id" -> "Ventana",
      "video_name" -> "V20160922T030001Z",
      "uri" -> "http://www.mbari.org/movies/anothermovie.mp4",
      "start_timestamp" -> "2016-09-22T03:00:01Z",
      "duration_millis" -> "90000",
      "container" -> "video/mp4",
      "video_codec" -> "h264",
      "audio_codec" -> "aac",
      "width" -> "1920",
      "height" -> "1080",
      "frame_rate" -> "60.07",
      "size_bytes" -> "12233456",
      "video_description" -> "A test movie",
      "sha512" -> sha512) {
        status should be(200)
        val media = gson.fromJson(body, classOf[Media])
        media.videoSequenceUuid should not be (null)
        media.videoUuid should not be (null)
        media.videoReferenceUuid should not be (null)
        println(body)
      }
  }

  it should "find by sha512" in {
    val sha512 = ByteArrayConverter.encode(Array.fill[Byte](64)(11))
    get(s"/v1/media/sha512/$sha512") {
      status should be(200)
      val media = gson.fromJson(body, classOf[Media])
      media.videoSequenceUuid should not be (null)
      media.videoUuid should not be (null)
      media.videoReferenceUuid should not be (null)

      val thatSha = ByteArrayConverter.encode(media.sha512)
      thatSha should be(sha512)
    }
  }

  it should "find by video sequence name" in {
    get(s"/v1/media/$name") {
      status should be(200)
      val media = gson.fromJson(body, classOf[Array[Media]])
      media.size should be(1)
      media(0).videoSequenceName should be(name)
    }
  }

  it should "findByCameraIdandTimestamp" in {
    get(s"/v1/media/camera/Ventana/1968-09-22T03:00:01Z") {
      status should be(200)
      val media = gson.fromJson(body, classOf[Array[Media]])
      media.size should be(1)
    }
  }
}