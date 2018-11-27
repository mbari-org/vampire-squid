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

import java.net.URLEncoder
import java.time.Instant
import java.util.Base64

import org.mbari.vars.vam.controllers.{ VideoController, VideoReferenceController, VideoSequenceController }
import org.mbari.vars.vam.dao.jpa.{ Video, VideoReference, VideoSequence }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-08-12T15:24:00
 */
class VideoReferenceV1ApiSpec extends WebApiStack {

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
    aVideoSequence should not be null

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
    aVideo should not be null

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
    aVideoReference should not be null
  }

  it should "get by uuid" in {
    get("/v1/videoreference/" + aVideoReference.uuid) {
      status should be(200)
      val videoReference = gson.fromJson(body, classOf[VideoReference])
      videoReference.uuid should be(aVideoReference.uuid)
    }
  }

  it should "get by uri" in {
    val uri =
      get("/v1/videoreference/uri/" +
        URLEncoder.encode(aVideoReference.uri.toURL.toExternalForm, "UTF-8")) {
        status should be(200)
      }
  }

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
