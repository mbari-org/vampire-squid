package org.mbari.vars.vam.api

import java.time.{ Duration, Instant }
import java.util.UUID

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.controllers.VideoController
import org.mbari.vars.vam.dao.jpa.{ Video, VideoSequence }
import org.scalatra.{ BadRequest, NoContent, NotFound }
import org.scalatra.swagger.{ DataType, ParamType, Parameter, Swagger }

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._
import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-24T13:41:00
 */
class VideoV1Api(controller: VideoController)(implicit val swagger: Swagger, val executor: ExecutionContext)
    extends APIStack {
  override protected def applicationDescription: String = "Video API (v1)"
  override protected val applicationName: Option[String] = Some("VideoAPI")

  before() {
    contentType = "application/json"
    response.headers += ("Access-Control-Allow-Origin" -> "*")
  }

  val vGET = (apiOperation[Iterable[Video]]("findAll")
    summary "List all videos")

  get("/?", operation(vGET)) {
    controller.findAll
      .map(_.asJava)
      .map(controller.toJson)
  }

  val uuidGET = (apiOperation[Video]("findByUUID")
    summary "Find a video by uuid"
    parameters (
      pathParam[UUID]("uuid").description("The UUID of the video")))

  get("/:uuid", operation(uuidGET)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller.findByUUID(uuid).map({
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a UUID of $uuid was not found in the database"))
      case Some(v) => controller.toJson(v)
    })
  }

  val nameGET = (apiOperation[Video]("findByName")
    summary "Find a video by name"
    parameters (
      pathParam[String]("name").description("The name of the video")))

  get("/name/:name", operation(nameGET)) {
    val name = params.get("name").getOrElse(halt(BadRequest("Please provide a name")))
    controller.findByName(name).map({
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a name of '$name' was not found in the database"))
      case Some(v) => controller.toJson(v)
    })
  }

  val timestampGET = (apiOperation[Iterable[Video]]("findByTimestamp")
    summary "Find videos by timestamp"
    parameters (
      pathParam[String]("timestamp").description("A UTC timestamp (yyyy-mm-ddThh:mm:ssZ)"),
      Parameter("window_minutes", DataType.Long, Some("The search windows in minutes"), required = false,
        defaultValue = Some(Constants.DEFAULT_DURATION_WINDOW.toMinutes.toString))))

  get("/timestamp/:timestamp", operation(timestampGET)) {
    val timestamp = params.getAs[Instant]("timestamp").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'timestamp' parameter is required")))
    val window = Try(Duration.ofMinutes(params.getAs[Long]("window_minutes").get))
      .getOrElse(Constants.DEFAULT_DURATION_WINDOW)
    controller.findByTimestamp(timestamp, window)
      .map(controller.toJson)
  }

  val timerangeGET = (apiOperation[Iterable[Video]]("findBetweenTimestamps")
    summary "Find videos between timestamps"
    parameters (
      pathParam[String]("start").description("A UTC timestamp (yyyy-mm-ddThh:mm:ssZ)"),
      pathParam[String]("end").description("A UTC timestamp (yyyy-mm-ddThh:mm:ssZ)")))

  get("/timestamp/:start/:end", operation(timerangeGET)) {
    val startTime = params.getAs[Instant]("start").getOrElse(halt(BadRequest(
      body = "{}",
      reason = " A 'start' parameter is required")))
    val endTime = params.getAs[Instant]("end").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "An 'end' parameter is required")))
    controller.findBetweenTimestamps(startTime, endTime)
      .map(_.asJava)
      .map(controller.toJson)
  }

  // TODO delete should require authentication
  val vDELETE = (apiOperation[Unit]("delete")
    summary "Delete a video. Also deletes associated video-references"
    parameters (
      pathParam[UUID]("uuid").description("The UUID of the video to be deleted")))

  delete("/:uuid", operation(vDELETE)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))
    controller.delete(uuid).map({
      case true => halt(NoContent(reason = s"Success! Deleted video with UUID of $uuid"))
      case false => halt(NotFound(reason = s"Failed. No video with UUID of $uuid was found."))
    })
  }

  // TODO create should require authentication
  val vPOST = (apiOperation[String]("create")
    summary "Create a video"
    parameters (
      Parameter("name", DataType.String, Some("The unique name of the video"), None, ParamType.Body, required = true),
      Parameter("video_sequence_uuid", DataType.String, Some("The uuid of the owning video-sequence"), None, ParamType.Body, required = true),
      Parameter("start", DataType.String, Some("The start time of the video as 'yyyy-mm-ddThh:mm:ssZ'"), None, ParamType.Body, required = true),
      Parameter("duration_millis", DataType.Long, Some("The duration of the video in milliseconds"), None, ParamType.Body, required = false),
      Parameter("description", DataType.String, Some("A description of the video"), None, ParamType.Body, required = false)))

  post("/", operation(vPOST)) {
    val name = params.get("name").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'name' parameter is required")))
    val videoSequenceUUID = params.getAs[UUID]("video_sequence_uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'video_sequence_uuid' parameter is required")))
    val start = params.getAs[Instant]("start").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'start' parameter is required")))
    val duration = params.getAs[Duration]("duration_millis")
    val description = params.get("description")
    controller.create(videoSequenceUUID, name, start, duration, description)
      .map(controller.toJson)
  }

  // TODO update should require authentication
  val vPUT = (apiOperation[Video]("update")
    summary "Update a video"
    parameters (
      pathParam[UUID]("The UUID of the video to be updated"),
      Parameter("name", DataType.String, Some("The unique name of the video"), None, ParamType.Body, required = false),
      Parameter("video_sequence_uuid", DataType.String, Some("The uuid of the owning video-sequence"), None, ParamType.Body, required = false),
      Parameter("start", DataType.String, Some("The start time of the video as 'yyyy-mm-ddThh:mm:ssZ'"), None, ParamType.Body, required = false),
      Parameter("duration_millis", DataType.Long, Some("The duration of the video in milliseconds"), None, ParamType.Body, required = false),
      Parameter("description", DataType.String, Some("A description of the video"), None, ParamType.Body, required = false)))

  put("/:uuid", operation(vPUT)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))
    val name = params.get("name")
    val description = params.get("description")
    val start = params.getAs[Instant]("start")
    val duration = params.getAs[Duration]("duration_millis")
    val videoSequenceUUID = params.getAs[UUID]("video_sequence_uuid")
    controller.update(uuid, name, start, duration, description, videoSequenceUUID)
      .map(controller.toJson)
  }

}
