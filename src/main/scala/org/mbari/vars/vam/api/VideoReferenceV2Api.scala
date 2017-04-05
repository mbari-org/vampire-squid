package org.mbari.vars.vam.api

import java.net.URI
import java.util.UUID
import javax.persistence.{ Column, Convert }

import com.google.gson.annotations.Expose
import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.controllers.VideoReferenceController
import org.mbari.vars.vam.dao.jpa.{ ByteArrayConverter, VideoReference }
import org.scalatra.{ BadRequest, NoContent, NotFound }
import org.scalatra.swagger.{ DataType, ParamType, Parameter, Swagger }

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._
import scala.util.Try

/**
 * @author Brian Schlining
 * @since 2017-04-05T08:32:00
 */
class VideoReferenceV2Api(controller: VideoReferenceController)(implicit val swagger: Swagger, val executor: ExecutionContext)
    extends APIStack {

  override protected def applicationDescription: String = "Video Reference API (v2)"

  override protected val applicationName: Option[String] = Some("VideoReferenceV2API")

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

  get("/uri/:uri", operation(uriGET)) {
    val uri = params.getAs[URI]("uri").getOrElse(halt(BadRequest("Please provide a URI")))
    controller.findByURI(uri).map({
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a URI of $uri was not found in the database"))
      case Some(v) => controller.toJson(v)
    })
  }

  val shaGET = (apiOperation[VideoReference]("findBySha512")
    summary "Find a video-reference by checksum (SHA512)"
    parameters (
      pathParam[String]("sha512").description("The Base64 encoded SHA512 of the video-reference")))

  get("/sha512/:sha512", operation(shaGET)) {
    val sha = params.get("sha512")
      .map(s => ByteArrayConverter.decode(s))
      .getOrElse(halt(BadRequest("Please provide a Base64 encoded sha512 checksum")))
    controller.findBySha512(sha).map {
      case None => halt(NotFound(
        body = "{}",
        reason = s"A video with a SHA512 checksum of $sha was not found in the database"))
      case Some(vr) => controller.toJson(vr)
    }
  }

  val vrDELETE = (apiOperation[Unit]("delete")
    summary "Delete a video-reference."
    parameters (
      pathParam[UUID]("uuid").description("The UUID of the video-reference to be deleted")))

  delete("/:uuid", operation(vrDELETE)) {
    validateRequest()
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))
    controller.delete(uuid).map({
      case true => halt(NoContent(reason = s"Success! Deleted video with UUID of $uuid"))
      case false => halt(NotFound(reason = s"Failed. No video with UUID of $uuid was found."))
    })
  }

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
      Parameter("description", DataType.String, Some("A description of the video"), None, ParamType.Body, required = false),
      Parameter("sha512", DataType.String, Some("The SHA512 checksum of the video (base 64 encoded)"), None, ParamType.Body, required = false)))

  post("/", operation(vPOST)) {
    validateRequest()
    val body = readBody(request)
    val (uuid, videoReference) = request.getHeader("Content-Type").toLowerCase match {
      case "application/json" => {
        val vrp = Constants.GSON.fromJson(body, classOf[VideoRefParams])
        (Option(vrp.videoUuid), vrp)
      }
      case _ => formToVideoReference(body)
    }

    val videoUuid = uuid.getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A 'video_uuid' parameter is required.")))
    val uri = Option(videoReference.uri)
      .getOrElse(halt(BadRequest(
        body = "{}",
        reason = "A 'uri' parameters is required.")))

    controller.create(
      videoUuid,
      uri,
      Option(videoReference.container),
      Option(videoReference.videoCodec),
      Option(videoReference.audioCodec),
      Option(videoReference.width),
      Option(videoReference.height),
      Option(videoReference.frameRate),
      Option(videoReference.size),
      Option(videoReference.description),
      Option(videoReference.sha512)).map(controller.toJson)
  }

  private def formToVideoReference(body: String): (Option[UUID], VideoReference) = {
    val args = parsePostBody(body).toMap
    val vr = new VideoReference
    val uuid = args.get("video_uuid")
      .flatMap(stringToUUID(_))
    args.get("uri")
      .flatMap(stringToURI(_))
      .foreach(vr.uri = _)
    args.get("description").foreach(vr.description = _)
    args.get("container").foreach(vr.container = _)
    args.get("video_codec").foreach(vr.videoCodec = _)
    args.get("audio_codec").foreach(vr.audioCodec = _)
    args.get("width")
      .map(s => Try(s.toInt).toOption)
      .foreach(opt => opt.foreach(vr.width = _))
    args.get("height")
      .map(s => Try(s.toInt).toOption)
      .foreach(opt => opt.foreach(vr.height = _))
    args.get("frame_rate")
      .map(s => Try(s.toDouble).toOption)
      .foreach(opt => opt.foreach(vr.frameRate = _))
    args.get("size_bytes")
      .map(s => Try(s.toLong).toOption)
      .foreach(opt => opt.foreach(vr.size = _))
    args.get("sha512")
      .flatMap(stringToByteArray(_))
      .foreach(vr.sha512 = _)
    (uuid, vr)
  }

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
      Parameter("description", DataType.String, Some("A description of the video"), None, ParamType.Body, required = false),
      Parameter("sha512", DataType.String, Some("The SHA512 checksum of the video (base 64 encoded)"), None, ParamType.Body, required = false)))

  put("/:uuid", operation(vPUT)) {
    validateRequest()
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest(
      body = "{}",
      reason = "A UUID parameter is required")))

    val body = readBody(request)
    val (videoUuid, videoReference) = request.getHeader("Content-Type").toLowerCase match {
      case "application/json" => {
        val vrp = Constants.GSON.fromJson(body, classOf[VideoRefParams])
        (Option(vrp.videoUuid), vrp)
      }
      case _ => formToVideoReference(body)
    }

    controller.update(
      uuid,
      videoUuid,
      Option(videoReference.uri),
      Option(videoReference.container),
      Option(videoReference.videoCodec),
      Option(videoReference.audioCodec),
      Option(videoReference.width),
      Option(videoReference.height),
      Option(videoReference.frameRate),
      Option(videoReference.size),
      Option(videoReference.description),
      Option(videoReference.sha512)).map(controller.toJson)
  }

}

/**
 * Same fileds as VideoReference plus videoUuid
 */
class VideoRefParams extends VideoReference {
  @Expose(serialize = true)
  var videoUuid: UUID = _
}