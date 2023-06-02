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

import org.mbari.vars.vam.controllers.VideoSequenceController
import org.mbari.vars.vam.dao.jpa.VideoSequenceEntity

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-08-10T15:28:00
  */
class VideoSequenceV1ApiSpec extends WebApiStack {

  private[this] val videoSequenceV1Api = {

    val videoSequenceController = new VideoSequenceController(daoFactory)
    new VideoSequenceV1Api(videoSequenceController)
  }

  addServlet(videoSequenceV1Api, "/v1/videosequence")

  "VideoSequenceV1API" should "return an empty JSON array when the database is empty" in {
    get("/v1/videosequence") {
      status should be(200)
      body should equal("[]")
    }
  }

  var aVideoSequence: VideoSequenceEntity = _

  it should "insert a videosequence" in {
    post("/v1/videosequence", "name" -> "T1234", "camera_id" -> "Tiburon") {
      status should be(200)
      body should include("uuid")
      body should include("name")
      body should include("camera_id")
      body should include("videos")
      val videoSequence = gson.fromJson(body, classOf[VideoSequenceEntity])
      videoSequence should not be (null)
      videoSequence.name should be("T1234")
      videoSequence.cameraID should be("Tiburon")
      videoSequence.videos shouldBe empty
      videoSequence.uuid should not be (null)
      aVideoSequence = videoSequence
    }
    post("/v1/videosequence", "name" -> "T2345", "camera_id" -> "Tiburon") {
      status should be(200)
    }
    post("/v1/videosequence", "name" -> "T3456", "camera_id" -> "Tiburon") {
      status should be(200)
    }
    post("/v1/videosequence", "name" -> "V0001", "camera_id" -> "Ventana") {
      status should be(200)
    }
  }

  it should "get by uuid" in {
    get("/v1/videosequence/" + aVideoSequence.uuid) {
      status should be(200)
      val videoSequence = gson.fromJson(body, classOf[VideoSequenceEntity])
      videoSequence should equal(aVideoSequence)
    }
  }

  it should "get by name" in {
    get("/v1/videosequence/name/T1234") {
      status should be(200)
      body should include("uuid")
      body should include("name")
      body should include("camera_id")
      body should include("videos")
      val videoSequence = gson.fromJson(body, classOf[VideoSequenceEntity])
      videoSequence should equal(aVideoSequence)
    }
  }

  it should "return 404 if the names is not found" in {
    get("/v1/videosequence/name/Zebra") {
      status should be(404)
    }
  }

  it should "find all names used" in {
    get("/v1/videosequence/names") {
      status should be(200)
      body should include("T1234")
    }
  }

  it should "find names by camera" in {
    get("/v1/videosequence/names/camera/Tiburon") {
      status should be(200)
      body should include("T1234")
    }

  }

  it should "return all cameras used" in {
    get("/v1/videosequence/cameras") {
      status should be(200)
      body should include("Tiburon")
      body should include("Ventana")
    }
  }

  it should "return a lastupdated time" in {
    get(s"/v1/videosequence/lastupdate/${aVideoSequence.uuid}") {
      status should be(200)
      body should include("timestamp")
    }
  }

  it should "update" in {
    put("/v1/videosequence/" + aVideoSequence.uuid, "description" -> "updated") {
      status should be(200)
      val videoSequence = gson.fromJson(body, classOf[VideoSequenceEntity])
      videoSequence.uuid should equal(aVideoSequence.uuid)
      videoSequence.description should be("updated")
    }
  }

  it should "delete" in {
    delete("/v1/videosequence/" + aVideoSequence.uuid) {
      status should be(204)
    }
    get("/v1/videosequence/" + aVideoSequence.uuid) {
      status should be(404)
    }
  }

}
