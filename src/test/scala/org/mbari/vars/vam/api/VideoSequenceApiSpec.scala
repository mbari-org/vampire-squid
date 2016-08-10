package org.mbari.vars.vam.api

import java.util.UUID

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.controllers.{VideoController, VideoReferenceController, VideoSequenceController}
import org.mbari.vars.vam.dao.jpa.{DevelopmentDAOFactory, VideoSequence}
import org.scalatest.FunSpecLike
import org.scalatra.swagger.{ApiInfo, Swagger}
import org.scalatra.test.scalatest.{ScalatraFlatSpec, ScalatraSuite}

import scala.concurrent.ExecutionContext

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-08-10T15:28:00
 */
class VideoSequenceApiSpec extends ScalatraFlatSpec {

  private[this] val gson = Constants.GSON

  private[this] val videoSequenceV1Api = {
    val apiInfo = ApiInfo(
      """video-asset-manager""",
      """Video Asset Manager - Server""",
      """http://localhost:8080/api-docs""",
      """brian@mbari.org""",
      """MIT""",
      """http://opensource.org/licenses/MIT""")

    implicit val swagger = new Swagger("1.2", "1.0.0", apiInfo)
    implicit val executionContext = ExecutionContext.global

    val daoFactory = DevelopmentDAOFactory
    val videoSequenceController = new VideoSequenceController(daoFactory)
    new VideoSequenceV1Api(videoSequenceController)
  }


  addServlet(videoSequenceV1Api, "/v1/videosequence")

  "VideoSequenceAPI" should "return an empty JSON array when the database is empty" in {
    get("/v1/videosequence") {
      status should be(200)
      body should equal("[]")
    }
  }

  var aVideoSequence: VideoSequence = _

  it should "insert a videosequence" in {
    post("/v1/videosequence", "name" -> "T1234", "camera_id" -> "Tiburon") {
      status should be (200)
      body should include("uuid")
      body should include("name")
      body should include("camera_id")
      body should include("videos")
      val videoSequence = gson.fromJson(body, classOf[VideoSequence])
      videoSequence should not be (null)
      videoSequence.name should be ("T1234")
      videoSequence.cameraID should be ("Tiburon")
      videoSequence.videos shouldBe empty
      videoSequence.uuid should not be (null)
      aVideoSequence = videoSequence
    }
    post("/v1/videosequence", "name" -> "T2345", "camera_id" -> "Tiburon") {}
    post("/v1/videosequence", "name" -> "T3456", "camera_id" -> "Tiburon") {}
    post("/v1/videosequence", "name" -> "V0001", "camera_id" -> "Ventana") {}
  }

  it should "find a videosequence by name" in {
    get("/v1/videosequence/name/T1234") {

    }
  }

}
