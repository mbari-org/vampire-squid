package org.mbari.vars.vam.api

import java.time.{ Duration, Instant }
import java.util.UUID
import java.util.{ HashMap => JHashMap }

import org.mbari.vars.vam.controllers.VideoSequenceController
import org.mbari.vars.vam.dao.jpa.VideoSequence
import org.scalatra.swagger.{ DataType, ParamType, Parameter, Swagger }
import org.slf4j.LoggerFactory
import org.scalatra._

import scala.concurrent.ExecutionContext
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
  //private[this] val textHeader = Map("Content-Type" -> "text/plain")

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
        body = "{}",
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
        halt(NotFound(body = "{}", reason = s"A video-sequence with a name of '$name' was not found in the database"))
      }
      case Some(vs) => controller.toJson(vs)
    })
  }

  get("/names") {
    controller.findAllNames
      .map(ns => Map("names" -> ns.asJava).asJava) // Transform to Java map for GSON
      .map(controller.toJson)
  }

  get("/cameras") {
    controller.findAllCameraIDs
      .map(cids => Map("camera_ids" -> cids.asJava).asJava) // Transform to Java map for GSON
      .map(controller.toJson)
  }

  get("/camera/:camera_id/:timestamp") {
    val cameraID = params.get("camera_id").getOrElse(halt(BadRequest(
      body = "{}",
      reason = " A 'camera_id' parameter is required")))
    val timestamp = params.getAs[Instant]("timestamp").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'timestamp' parameter is required")))
    // TODO add optional window parameter
    controller.findByCameraIDAndTimestamp(cameraID, timestamp, Duration.ofMinutes(30))
  }

  val vsPOST = (apiOperation[Unit]("createPOST")
    summary "Create a video-sequence"
    parameters (
      Parameter("name", DataType.String, Some("The unique name of the video-sequence"), None, ParamType.Body, required = true),
      Parameter("camera_id", DataType.String, Some("The name of the camera (e.g. Tiburon)"), None, ParamType.Body, required = true)))

  // TODO post should require authentication
  post("/", operation(vsPOST)) {
    val name = params.get("name").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'name' parameter is required")))
    val cameraID = params.get("camera_id").getOrElse(halt(BadRequest("A 'camera_id' parameter is required")))
    val description = params.get("description")
    controller.create(name, cameraID)
      .map(controller.toJson)
  }

  // TODO delete should require authentication
  val vsDELETE = (apiOperation[Unit]("videoSequenceDELETE")
    summary "Delete a video-sequence"
    parameters (
      pathParam[UUID]("uuid").description("The UUID of the video-sequence to be deleteds")))

  delete("/:uuid", operation(vsDELETE)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))
    controller.delete(uuid).map({
      case true => halt(NoContent(reason = s"Success! Deleted video-sequence with UUID of $uuid"))
      case false => halt(NotFound(reason = s"Failed. No video-sequence with UUID of $uuid was found."))
    })
  }

  // TODO put should update values. Should require authentication
  put("/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))

  }

}
