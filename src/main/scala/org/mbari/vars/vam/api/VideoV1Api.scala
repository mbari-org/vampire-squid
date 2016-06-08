package org.mbari.vars.vam.api

import java.time.{ Duration, Instant }
import java.util.UUID

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.controllers.VideoController
import org.scalatra.{ BadRequest, NoContent, NotFound }
import org.scalatra.swagger.Swagger

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

  get("/?") {
    controller.findAll
      .map(_.asJava)
      .map(controller.toJson)
  }

  get("/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller.findByUUID(uuid).map({
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a UUID of $uuid was not found in the database"))
      case Some(v) => controller.toJson(v)
    })
  }

  get("/name/:name") {
    val name = params.get("name").getOrElse(halt(BadRequest("Please provide a name")))
    controller.findByName(name).map({
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a name of '$name' was not found in the database"))
      case Some(v) => controller.toJson(v)
    })
  }

  get("/timestamp/:timestamp") {
    val timestamp = params.getAs[Instant]("timestamp").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'timestamp' parameter is required")))
    val window = Try(Duration.ofMinutes(params.getAs[Long]("window_minutes").get))
      .getOrElse(Constants.DEFAULT_DURATION_WINDOW)
    controller.findByTimestamp(timestamp, window)
      .map(controller.toJson)
  }

  get("/timestamp/:start/:end") {
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

  delete("/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))
    controller.delete(uuid).map({
      case true => halt(NoContent(reason = s"Success! Deleted video with UUID of $uuid"))
      case false => halt(NotFound(reason = s"Failed. No video with UUID of $uuid was found."))
    })
  }

  post("/") {
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

  put("/:uuid") {
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
