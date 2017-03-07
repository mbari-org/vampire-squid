package org.mbari.vars.vam.api

import org.mbari.vars.vam.controllers.MediaController
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
}
