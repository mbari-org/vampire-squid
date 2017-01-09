package org.mbari.vars.vam.api

import java.net.URI
import java.util.UUID

import org.mbari.vars.vam.controllers.VideoReferenceController
import org.mbari.vars.vam.dao.jpa.{ Video, VideoReference, VideoSequence }
import org.scalatra.{ BadRequest, NoContent, NotFound }
import org.scalatra.swagger.{ DataType, ParamType, Parameter, Swagger }

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

  val vsGET = (apiOperation[Iterable[VideoReference]]("findAll")
    summary "List all video-references")

  get("/?", operation(vsGET)) {
    controller.findAll.map(vs => controller.toJson(vs.asJava))
  }

  val uuidGET = (apiOperation[VideoReference]("findByUUID")
    summary "Find a video-reference by uuid"
    parameters (
      pathParam[UUID]("uuid").description("The UUID of the video-reference")))

  get("/:uuid", operation(uuidGET)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller.findByUUID(uuid).map({
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a UUID of $uuid was not found in the database"))
      case Some(v) => controller.toJson(v)
    })
  }

  val uriGET = (apiOperation[VideoReference]("findByURI")
    summary "Find a video-reference by its URI"
    parameters (
      pathParam[URI]("uuid").description("The URI of the video-reference")))

  // TODO encode/decode URL
  get("/uri/:uri", operation(uriGET)) {
    val uri = params.getAs[URI]("uri").getOrElse(halt(BadRequest("Please provide a URI")))
    controller.findByURI(uri).map({
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a URI of $uri was not found in the database"))
      case Some(v) => controller.toJson(v)
    })
  }

  // TODO delete should require authentication
  val vrDELETE = (apiOperation[Unit]("delete")
    summary "Delete a video-reference."
    parameters (
      pathParam[UUID]("uuid").description("The UUID of the video-reference to be deleted")))

  delete("/:uuid", operation(vrDELETE)) {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))
    controller.delete(uuid).map({
      case true => halt(NoContent(reason = s"Success! Deleted video with UUID of $uuid"))
      case false => halt(NotFound(reason = s"Failed. No video with UUID of $uuid was found."))
    })
  }

  // TODO Create should require authentication
  val vPOST = (apiOperation[String]("create")
    summary "Create a video-reference"
    parameters (
      Parameter("video_uuid", DataType.String, Some("The uuid of the owning video"), None, ParamType.Body, required = true),
      Parameter("uri", DataType.String, Some("The unique URI of the video"), None, ParamType.Body, required = true),
      Parameter("container", DataType.String, Some("The container mimetype"), None, ParamType.Body, required = false),
      Parameter("video_codec", DataType.String, Some("An identifier for the video codec"), None, ParamType.Body, required = false),
      Parameter("audio_codec", DataType.String, Some("An identifier for the audio codec"), None, ParamType.Body, required = false),
      Parameter("width", DataType.Int, Some("The video's width in pixels"), None, ParamType.Body, required = false),
      Parameter("height", DataType.Int, Some("The video's height in pixels"), None, ParamType.Body, required = false),
      Parameter("frame_rate", DataType.Double, Some("The frame-rate of the video in frames per second"), None, ParamType.Body, required = false),
      Parameter("size_bytes", DataType.Long, Some("The size of the video in bytes"), None, ParamType.Body, required = false),
      Parameter("description", DataType.String, Some("A description of the video"), None, ParamType.Body, required = false)))

  post("/", operation(vPOST)) {
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

  // TODO update should require authentication
  val vPUT = (apiOperation[String]("update")
    summary "Update a video-reference"
    parameters (
      pathParam[UUID]("uuid").description("The UUID of the video-reference to be updated"),
      Parameter("video_uuid", DataType.String, Some("The uuid of the owning video"), None, ParamType.Body, required = false),
      Parameter("uri", DataType.String, Some("The unique URI of the video"), None, ParamType.Body, required = false),
      Parameter("container", DataType.String, Some("The container mimetype"), None, ParamType.Body, required = false),
      Parameter("video_codec", DataType.String, Some("An identifier for the video codec"), None, ParamType.Body, required = false),
      Parameter("audio_codec", DataType.String, Some("An identifier for the audio codec"), None, ParamType.Body, required = false),
      Parameter("width", DataType.Int, Some("The video's width in pixels"), None, ParamType.Body, required = false),
      Parameter("height", DataType.Int, Some("The video's height in pixels"), None, ParamType.Body, required = false),
      Parameter("frame_rate", DataType.Double, Some("The frame-rate of the video in frames per second"), None, ParamType.Body, required = false),
      Parameter("size_bytes", DataType.Long, Some("The size of the video in bytes"), None, ParamType.Body, required = false),
      Parameter("description", DataType.String, Some("A description of the video"), None, ParamType.Body, required = false)))

  put("/:uuid", operation(vPUT)) {
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
