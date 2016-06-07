package org.mbari.vars.vam.api

import java.net.URI
import java.util.UUID

import org.mbari.vars.vam.controllers.VideoReferenceController
import org.mbari.vars.vam.dao.jpa.VideoSequence
import org.scalatra.{ BadRequest, NoContent, NotFound }
import org.scalatra.swagger.Swagger

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-06-06T16:27:00
 */
class VideoReferenceV1Api(controller: VideoReferenceController)(implicit val swagger: Swagger, val executor: ExecutionContext)
    extends APIStack {

  override protected def applicationDescription: String = "Video Reference API (v1)"

  override protected val applicationName: Option[String] = Some("VideoReferenceAPI")

  before() {
    contentType = "application/json"
    response.headers += ("Access-Control-Allow-Origin" -> "*")
  }

  val vsGET = (apiOperation[Iterable[VideoSequence]]("findAll")
    summary "List all video sequences")

  get("/?", operation(vsGET)) {
    controller.findAll.map(vs => controller.toJson(vs.asJava))
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

  get("/uri/:uri") {
    val uri = params.getAs[URI]("uri").getOrElse(halt(BadRequest("Please provide a URI")))
    controller.findByURI(uri).map({
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a URI of $uri was not found in the database"))
      case Some(v) => controller.toJson(v)
    })
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
    val videoUUID = params.getAs[UUID]("video_uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'video_uuid' parameter is required.")))
    val uri = params.getAs[URI]("uri").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'uri' parameters is required.")))
    val description = params.get("description")
    val container = params.get("container")
    val videoCodec = params.get("video_codec")
    val audioCodec = params.get("audio_codec")
    val width = params.getAs[Int]("width")
    val height = params.getAs[Int]("height")
    val frameRate = params.getAs[Double]("frame_rate")
    val sizeBytes = params.getAs[Long]("size_bytes")
    controller.create(videoUUID, uri, container, videoCodec, audioCodec, width, height, frameRate,
      sizeBytes, description).map(controller.toJson)
  }

  put("/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))
    val videoUUID = params.getAs[UUID]("video_uuid")
    val uri = params.getAs[URI]("uri")
    val description = params.get("description")
    val container = params.get("container")
    val videoCodec = params.get("video_codec")
    val audioCodec = params.get("audio_codec")
    val width = params.getAs[Int]("width")
    val height = params.getAs[Int]("height")
    val frameRate = params.getAs[Double]("frame_rate")
    val sizeBytes = params.getAs[Long]("size_bytes")

    controller.update(uuid, videoUUID, uri, container, videoCodec, audioCodec, width, height,
      frameRate, sizeBytes, description).map(controller.toJson)

  }

}
