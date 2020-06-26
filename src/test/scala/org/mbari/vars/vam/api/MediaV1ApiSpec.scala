/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vars.vam.api

import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.dao.jpa.ByteArrayConverter
import org.mbari.vars.vam.model.Media
import java.net.URLEncoder
import java.net.URI
import scala.concurrent.Await
import scala.concurrent.duration._
import java.time.Instant

/**
  * @author Brian Schlining
  * @since 2017-03-06T17:20:00
  */
class MediaV1ApiSpec extends WebApiStack {

  private[this] val mediaController = new MediaController(daoFactory)
  private[this] val mediaV1Api      = new MediaV1Api(mediaController)
  private[this] val name            = getClass.getSimpleName

  addServlet(mediaV1Api, "/v1/media")

  "MediaV1Api" should "create w/ minimal args" in {
    post(
      "v1/media",
      "video_sequence_name" -> name,
      "camera_id"           -> "Ventana",
      "video_name"          -> "V19680922T030001Z",
      "uri"                 -> "http://www.mbari.org/movies/somemovie.mov",
      "start_timestamp"     -> "1968-09-22T03:00:01Z"
    ) {
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

    post(
      "/v1/media",
      "video_sequence_name" -> s"$name-bob",
      "camera_id"           -> "Ventana",
      "video_name"          -> "V20160922T030001Z",
      "uri"                 -> "http://www.mbari.org/movies/anothermovie.mp4",
      "start_timestamp"     -> "2016-09-22T03:00:01Z",
      "duration_millis"     -> "90000",
      "container"           -> "video/mp4",
      "video_codec"         -> "h264",
      "audio_codec"         -> "aac",
      "width"               -> "1920",
      "height"              -> "1080",
      "frame_rate"          -> "60.07",
      "size_bytes"          -> "12233456",
      "video_description"   -> "A test movie",
      "sha512"              -> sha512
    ) {
      status should be(200)
      val media = gson.fromJson(body, classOf[Media])
      media.videoSequenceUuid should not be (null)
      media.videoUuid should not be (null)
      media.videoReferenceUuid should not be (null)
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
      media.videoName should be("V20160922T030001Z")
      media.uri should be(URI.create("http://www.mbari.org/movies/anothermovie.mp4"))
      media.startTimestamp should be(Instant.parse("2016-09-22T03:00:01Z"))
      media.duration should be(java.time.Duration.ofMillis(90000))
      media.container should be("video/mp4")
      media.videoCodec should be("h264")
      media.audioCodec should be("aac")
      media.width should be(1920)
      media.height should be(1080)
      media.sizeBytes should be(12233456)
      media.description should be("A test movie")

      val thatSha = ByteArrayConverter.encode(media.sha512)
      thatSha should be(sha512)
    }
  }

  it should "find by URI" in {
    val uri = URI.create("http://www.mbari.org/movies/somemovie.mov")
    get("/v1/media/uri/" + URLEncoder.encode(uri.toString, "UTF-8")) {
      status should be(200)
      val media = gson.fromJson(body, classOf[Media])
      media.uri should be(uri)
      media.videoReferenceUuid should not be (null)
      media.videoUuid should not be (null)
      media.videoSequenceUuid should not be (null)
    }
  }

  it should "find by video sequence name" in {
    get(s"/v1/media/videosequence/$name") {
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

  it should "update with form body" in {
    val media = exec(mediaController.findByVideoName("V20160922T030001Z"))
    media should not be (null)
    media should not be empty
    val m      = media.head
    val sha512 = ByteArrayConverter.encode(m.sha512)
    put(
      "/v1/media",
      "sha512"              -> sha512,
      "video_sequence_name" -> m.videoSequenceName,
      "camera_id"           -> m.cameraId,
      "video_name"          -> m.videoName,
      "width"               -> "4000",
      "height"              -> "2000"
    ) {
      status should be(200)
      val um = gson.fromJson(body, classOf[Media])
      println(gson.toJson(um))
      um should not be (null)
      um.width should be(4000)
      um.height should be(2000)
    }
  }

  it should "update with uuid and form body" in {
    val media = exec(mediaController.findByVideoName("V20160922T030001Z"))
    media should not be (null)
    media should not be empty
    val m = media.head
    put(s"/v1/media/${m.videoReferenceUuid}", "width" -> "8000", "height" -> "4000") {
      status should be(200)
      val m2 = gson.fromJson(body, classOf[Media])
      m2.width should be(8000)
      m2.height should be(4000)
      m2.sha512 should be(m.sha512)
    }

  }
}
