package org.mbari.vars.vam.api

import java.nio.charset.Charset

import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.dao.jpa.ByteArrayConverter
import org.mbari.vars.vam.model.Media
import org.slf4j.LoggerFactory

/**
 * @author Brian Schlining
 * @since 2017-04-04T16:07:00
 */
class MediaV2ApiSpec extends WebApiStack {

  private[this] val mediaV2Api = new MediaV2Api(new MediaController(daoFactory))
  private[this] val name = getClass.getSimpleName
  private[this] val log = LoggerFactory.getLogger(getClass)

  addServlet(mediaV2Api, "/v2/media")

  "MediaV2Api" should "create w/ minimal args" in {
    val b = "video_sequence_name=Brian&camera_id=Ventana&video_name=V19680922T030001Z" +
      "&uri=http://www.mbari.org/movies/somemovie.mov&start_timestamp=1968-09-22T03:00:01Z"
    post(
      "v2/media",
      body = b.getBytes(Charset.forName("UTF-8")),
      headers = Seq("Content-Type" -> "application/form")) {
        status should be(200)
        val media = gson.fromJson(body, classOf[Media])
        media.videoSequenceUuid should not be (null)
        media.videoUuid should not be (null)
        media.videoReferenceUuid should not be (null)
      }
  }

  it should "create w/ json" in {

    val sha512 = ByteArrayConverter.encode(Array.fill[Byte](64)(11))
    sha512.size should be(128)

    val b =
      s"""
        | {
        |  "video_sequence_name": "Brian",
        |  "camera_id": "Ventana",
        |  "uri": "http://www.mbari.org/movies/somemovie2.mov",
        |  "start": "1968-09-22T03:00:01Z",
        |  "video_name": "Brian foo",
        |  "width": 1920,
        |  "height": 1080,
        |  "frame_rate": 59.97,
        |  "size_bytes": 90000,
        |  "sha512": "$sha512"
        |}
      """.stripMargin

    post(
      "v2/media",
      body = b.getBytes(Charset.forName("UTF-8")),
      headers = Seq("Content-Type" -> "application/json")) {
        status should be(200)

        val media = gson.fromJson(body, classOf[Media])
        media.videoSequenceUuid should not be (null)
        media.videoUuid should not be (null)
        media.videoReferenceUuid should not be (null)
      }
  }
}
