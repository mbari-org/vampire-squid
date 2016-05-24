package org.mbari.vars.vam.api

import java.util.UUID

import org.mbari.vars.vam.controllers.VideoSequenceController
import org.mbari.vars.vam.dao.jpa.VideoSequence
import org.scalatra.swagger.{ Swagger, SwaggerSupport }
import org.slf4j.LoggerFactory
import org.scalatra._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal
import org.scalatra.util.conversion.TypeConverter
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
      case None => halt(NotFound(s"A video-sequence with a UUID of $uuid was not found in the database"))
      case Some(vs) => controller.toJson(vs)
    })
  }

  val nameGET = (apiOperation[VideoSequence]("videoSequenceByNameGET")
    summary "Find a video sequence by name"
    parameters (
      pathParam[String]("name").description("The name of the video sequence")))

  get("/name/:name", operation(nameGET)) {
    params.get("name") match {
      case Some(name) => controller.findByName(name).map({
        case Some(vs) => controller.toJson(vs)
        case None => NotFound
      })
      case None => BadRequest
    }
  }

  val vsPOST = (apiOperation[Unit]("createPOST")
    summary "Create a video sequence"
    parameters ())

  post("/") {
    val name = params("name")
    val cameraID = params("camera_id")
    if (name == null || cameraID == null) {
      UnprocessableEntity(reason = "Both name and cameraID parameters are required. One or both were missing")
    } else {
      try {
        controller.create(name, cameraID)
          .map(vs => Created(controller.toJson(vs), headers = Map("Location" -> s"/${vs.uuid}")))
      } catch {
        case NonFatal(e) => BadRequest(reason = "Something bad happened. " + e.getMessage)
      }
    }

  }

  delete("/:uuid") {
    params.getAs[UUID]("uuid") match {
      case Some(uuid) => controller.delete(uuid).map({
        case true => NoContent
        case false => NotFound
      })
      case None => BadRequest(reason = "No UUID was found.")
    }
  }

  put("/:uuid") {

  }

}
