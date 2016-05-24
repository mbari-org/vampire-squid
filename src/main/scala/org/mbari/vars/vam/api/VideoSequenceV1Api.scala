package org.mbari.vars.vam.api

import java.util.UUID

import org.mbari.vars.vam.controllers.VideoSequenceController
import org.mbari.vars.vam.dao.jpa.VideoSequence
import org.scalatra.swagger.{ Swagger }
import org.slf4j.LoggerFactory
import org.scalatra._

import scala.concurrent.{ ExecutionContext }
import scala.collection.JavaConverters._

/**
 * Note that we're not using Scalatra's JSON support. We're rolling out own with GSON
 *
 * @author Brian Schlining
 * @since 2016-05-20T14:45:00
 */
class VideoSequenceV1Api(controller: VideoSequenceController)(implicit val swagger: Swagger, val executor: ExecutionContext)
    extends APIStack {

  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val textHeader = Map("Content-Type" -> "text/plain")

  override protected def applicationDescription: String = "Video Sequence API (v1)"

  override protected val applicationName: Option[String] = Some("VideoSequenceAPI")

  before() {
    contentType = "application/json"
    response.headers += ("Access-Control-Allow-Origin" -> "*")
  }

  val vsGET = (apiOperation[Iterable[VideoSequence]]("videoSequenceGET")
    summary "List all video sequences")

  get("/?", operation(vsGET)) {
    controller.findAll.map(vs => controller.toJson(vs.asJava))
  }

  val uuidGET = (apiOperation[VideoSequence]("videoSequenceByUUIDGET")
    summary "Find a video sequence by uuid"
    parameters (
      pathParam[UUID]("uuid").description("The UUID of the video sequence")))

  get("/:uuid", operation(uuidGET)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a valid UUID")))
    controller.findByUUID(uuid).map({
      case None => halt(NotFound(
        headers = textHeader,
        reason = s"A video-sequence with a UUID of $uuid was not found in the database"))
      case Some(vs) => controller.toJson(vs)
    })
  }

  val nameGET = (apiOperation[VideoSequence]("videoSequenceByNameGET")
    summary "Find a video sequence by name"
    parameters (
      pathParam[String]("name").description("The name of the video sequence")))

  get("/name/:name", operation(nameGET)) {
    val name = params("name")
    controller.findByName(name).map({
      case None => {
        halt(NotFound(s"A video-sequence with a name of '$name' was not found in the database", headers = textHeader))
      }
      case Some(vs) => controller.toJson(vs)
    })
  }

  val vsPOST = (apiOperation[Unit]("createPOST")
    summary "Create a video sequence"
    parameters ())

  // TODO post should require authentication
  post("/") {
    val name = params.get("name").getOrElse(halt(BadRequest("A 'name' parameter is required", headers = textHeader)))
    val cameraID = params.get("camera_id").getOrElse(halt(BadRequest("A 'camera_id' parameter is required")))
    controller.create(name, cameraID)
      .map(controller.toJson)
  }

  // TODO delete should require authentication
  delete("/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("A UUID parameter is required", headers = textHeader)))
    controller.delete(uuid).map({
      case true => halt(NoContent(reason = s"Success! Deleted video-sequence with UUID of $uuid", headers = textHeader))
      case false => halt(NotFound(s"Failed. No video-sequence with UUID of $uuid was found.", headers = textHeader))
    })
  }

  // TODO put should update values
  put("/:uuid") {

  }

}
