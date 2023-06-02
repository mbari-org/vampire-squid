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

import java.time.Instant
import java.util

import org.mbari.vars.vam.controllers.{VideoController, VideoSequenceController}
import org.mbari.vars.vam.dao.jpa.{VideoEntity, VideoSequenceEntity}

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-08-11T17:05:00
  */
class VideoV1ApiSpec extends WebApiStack {

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

  //  "VideoV1Api" should "return an empty JSON array when the database is empty" in {
  //    get("/v1/video") {
  //      status should be(200)
  //      body should equal("[]")
  //    }
  //  }

  val startDate                     = Instant.now()
  var aVideoSequence: VideoSequenceEntity = _
  var aVideo: VideoEntity                 = _
  "VideoV1Api" should "insert" in {
    post("/v1/videosequence", "name" -> "T1234", "camera_id" -> "Tiburon") {
      status should be(200)
      aVideoSequence = gson.fromJson(body, classOf[VideoSequenceEntity])
    }
    aVideoSequence should not be null

    post(
      "/v1/video",
      "name"                -> "T1234-01",
      "video_sequence_uuid" -> aVideoSequence.uuid.toString,
      "start"               -> startDate.toString,
      "duration_millis"     -> s"${15 * 60 * 1000}"
    ) {
      status should be(200)
      body should include("name")
      body should include("uuid")
      body should include("start")
      body should include("duration_millis")
      aVideo = gson.fromJson(body, classOf[VideoEntity])
    }
    aVideo should not be null
  }

  it should "get a videosequence by video UUID" in {
    get("/v1/video/videosequence/" + aVideo.uuid) {
      status should be(200)
      val videoSequence = gson.fromJson(body, classOf[VideoSequenceEntity])
      videoSequence should equal(aVideoSequence)
      videoSequence.videos.map(_.uuid) should contain(aVideo.uuid)
    }
  }

  it should "get by name" in {
    get("/v1/video/name/" + aVideo.name) {
      status should be(200)
      val video = gson.fromJson(body, classOf[VideoEntity])
      video.uuid should equal(aVideo.uuid)
      video.name should equal(aVideo.name)
    }
  }

  it should "return a lastupdated time" in {
    get(s"/v1/video/lastupdate/${aVideo.uuid}") {
      status should be(200)
      body should include("timestamp")
    }
  }

  it should "get by timestamp" in {
    get("/v1/video/timestamp/" + startDate.plusMillis(1000)) {
      status should be(200)
      val video = gson.fromJson(body, classOf[util.ArrayList[VideoEntity]])
      video should not be empty
    }
  }

  it should "get between timestamps" in {
    get("/v1/video/timestamp/" + startDate.minusMillis(1000) + "/" + startDate.plusMillis(10000)) {
      status should be(200)
      val video = gson.fromJson(body, classOf[util.ArrayList[VideoEntity]])
      video should not be empty
    }
  }

  it should "update" in {
    put("/v1/video/" + aVideo.uuid, "description" -> "a description") {
      status should be(200)
      val video = gson.fromJson(body, classOf[VideoEntity])
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
