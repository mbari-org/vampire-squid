package org.mbari.vars.vam.api

import java.net.URI
import java.time.{ Duration, Instant }
import javax.servlet.http.HttpServletRequest

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.controllers.MediaController
import org.mbari.vars.vam.dao.jpa.ByteArrayConverter
import org.mbari.vars.vam.model.Media
import org.scalatra.{ BadRequest, NotFound }
import org.scalatra.swagger.Swagger

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.Try
import scala.util.control.NonFatal

/**
 * @author Brian Schlining
 * @since 2017-04-03T17:05:00
 */
class MediaV2Api(controller: MediaController)(implicit val swagger: Swagger, val executor: ExecutionContext)
    extends APIStack {

  override protected def applicationDescription: String = "Media API (v2)"

  override protected val applicationName: Option[String] = Some("MediaAPI v2")

  // https://stackoverflow.com/questions/9911801/how-to-get-the-body-of-post-request-in-scalatra
  // http://alvinalexander.com/scala/how-to-access-post-request-data-with-scalatra
  // https://stackoverflow.com/questions/611906/http-post-with-url-query-parameters-good-idea-or-not
  post("/") {
    validateRequest()
    val body = readBody(request)
    //println("BODY " + body)
    //println("HEADER " + request.getHeader("Content-Type"))
    val media = request.getHeader("Content-Type").toLowerCase match {
      case "application/json" => Constants.GSON.fromJson(body, classOf[Media])
      case _ => formToMedia(body)
    }

    Option(media.videoSequenceName)
      .orElse(halt(BadRequest("{}", reason = "A 'video_sequence_name' param is required")))
    Option(media.cameraId)
      .orElse(halt(BadRequest("{}", reason = "A 'video_sequence_name' param is required")))
    Option(media.videoName)
      .orElse(halt(BadRequest("{}", reason = "A 'video_name' param is required")))
    Option(media.uri)
      .orElse(halt(BadRequest(body = "{}", reason = "A 'uri' parameters is required.")))
    Option(media.start)
      .orElse(halt(BadRequest("{}", reason = "A 'start_timestamp' param is required")))

    controller.create(
      media.videoSequenceName,
      media.cameraId,
      media.videoName,
      media.uri,
      media.start,
      Option(media.duration),
      Option(media.container),
      Option(media.videoCodec),
      Option(media.audioCodec),
      Option(media.width),
      Option(media.height),
      Option(media.frameRate),
      Option(media.sizeBytes),
      Option(media.description),
      Option(media.sha512)).map(controller.toJson)

  }

  def formToMedia(body: String): Media = {
    val args = parsePostBody(body).toMap
    val media = new Media
    args.get("video_sequence_name").foreach(media.videoSequenceName = _)
    args.get("camera_id").foreach(media.cameraId = _)
    args.get("video_name").foreach(media.videoName = _)
    args.get("start_timestamp")
      .flatMap(stringToInstant(_))
      .foreach(media.start = _)
    args.get("uri")
      .flatMap(stringToURI(_))
      .foreach(media.uri = _)
    args.get("duration_millis")
      .flatMap(stringToDuration(_))
      .foreach(media.duration = _)
    args.get("container").foreach(media.container = _)
    args.get("video_codec").foreach(media.videoCodec = _)
    args.get("audio_codec").foreach(media.audioCodec = _)
    args.get("width")
      .map(s => Try(s.toInt).toOption)
      .foreach(opt => opt.foreach(media.width = _))
    args.get("height")
      .map(s => Try(s.toInt).toOption)
      .foreach(opt => opt.foreach(media.height = _))
    args.get("frame_rate")
      .map(s => Try(s.toDouble).toOption)
      .foreach(opt => opt.foreach(media.frameRate = _))
    args.get("size_bytes")
      .map(s => Try(s.toLong).toOption)
      .foreach(opt => opt.foreach(media.sizeBytes = _))
    args.get("sha512")
      .flatMap(stringToByteArray(_))
      .foreach(media.sha512 = _)
    media
  }

  get("/sha512/:sha512") {
    val shaString = params.get("sha512")
      .getOrElse(halt(BadRequest("{}", reason = "A hex encoded SHA512 checksum is required")))
    val sha = ByteArrayConverter.decode(shaString)
    controller.findBySha512(sha).map({
      case None => halt(NotFound("{}", reason = s"A video with matching checksum was not found"))
      case Some(v) => controller.toJson(v)
    })
  }

  get("/:name") {
    val name = params.get("name")
      .getOrElse(halt(BadRequest("{}", reason = "A video sequence name parameter is required")))
    controller.findByVideoSequenceName(name)
      .map(_.asJava)
      .map(controller.toJson)
  }

  get("/camera/:camera_id/:datetime") {
    val cameraId = params.get("camera_id")
      .getOrElse(halt(BadRequest("{}", reason = "A 'camera id' parameters is required")))
    val ts = params.getAs[Instant]("datetime")
      .getOrElse(halt(BadRequest("", reason = "A datetime parameter in UTC time formated as 'yyyy-mm-ddThh:MM:ssZ' is required")))
    controller.findByCameraIdAndTimestamp(cameraId, ts)
      .map(_.asJava)
      .map(controller.toJson)
  }
}
