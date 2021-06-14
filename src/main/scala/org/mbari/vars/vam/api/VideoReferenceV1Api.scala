/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vars.vam.api

import java.net.URI
import java.util.UUID

import org.mbari.vars.vam.controllers.VideoReferenceController
import org.mbari.vars.vam.dao.jpa.{ByteArrayConverter, VideoReference}
import org.scalatra.{BadRequest, NoContent, NotFound}
import org.scalatra.swagger.{DataType, ParamType, Parameter, Swagger}

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import org.slf4j.LoggerFactory

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-06-06T16:27:00
  */
class VideoReferenceV1Api(controller: VideoReferenceController)(
    implicit val executor: ExecutionContext
) extends APIStack {

  val log = LoggerFactory.getLogger(getClass())

  get("/?") {
    controller.findAll.map(vs => controller.toJson(vs.asJava))
  }


  get("/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller
      .findByUUID(uuid)
      .map({
        case None =>
          halt(
            NotFound(
              s"""{not_found: "A video with a UUID of $uuid was not found in the database"}"""
            )
          )
        case Some(v) => controller.toJson(v)
      })
  }

  get("/lastupdate/:uuid") {
    val uuid = params.getAs[UUID]("uuid").getOrElse(halt(BadRequest("Please provide a UUID")))
    controller
      .findByUUID(uuid)
      .map({
        case None =>
          val error = Map(
            "not_found" -> s"A video reference with a UUID of $uuid was not found in the database"
          ).asJava
          halt(NotFound(controller.toJson(error)))
        case Some(v) =>
          v.lastUpdated match {
            case None =>
              val error = Map(
                "missing_value"        -> "No last updated timestamp was found",
                "video_reference_uuid" -> uuid
              ).asJava
              halt(NotFound(controller.toJson(error)))
            case Some(t) =>
              val data = Map("timestamp" -> t.toString).asJava
              controller.toJson(data)
          }
      })
  }


  get("/uri/*") {

    val uri = params.getAs[URI]("splat").getOrElse(halt(BadRequest("Please provide a URI")))

    log.info(s"findByURI ... $uri")
    controller
      .findByURI(uri)
      .map({
        case None =>
          halt(
            NotFound(s"""{not_found: "A video with a URI of $uri was not found in the database"}""")
          )
        case Some(v) => controller.toJson(v)
      })
  }

  get("/uris") {
    controller.findAllURIs.map(uris => controller.toJson(uris.asJava))
  }


  get("/sha512/:sha512") {
    val sha = params
      .get("sha512")
      .map(s => ByteArrayConverter.decode(s))
      .getOrElse(halt(BadRequest("Please provide a Base64 encoded sha512 checksum")))
    controller.findBySha512(sha).map {
      case None =>
        halt(
          NotFound(
            s"""{not_found: "A video with a SHA512 checksum of '$sha' was not found in the database"}"""
          )
        )
      case Some(vr) => controller.toJson(vr)
    }
  }


  delete("/:uuid") {
    validateRequest()
    val uuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'uuid' parameter is required"}""")))
    controller
      .delete(uuid)
      .map({
        case true => halt(NoContent()) // Success! Deleted video with uuid
        case false =>
          halt(
            NotFound(
              s"""{not_found: "A video with a UUID of $uuid was not found in the database"}"""
            )
          )
      })
  }


  post("/") {
    validateRequest()
    val videoUUID = params
      .getAs[UUID]("video_uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'video_uuid' parameter is required"}""")))
    val uri = params
      .getAs[URI]("uri")
      .getOrElse(halt(BadRequest("""{error: "A 'uri' parameter is required"}""")))
    val description = params.get("description")
    val container   = params.get("container")
    val videoCodec  = params.get("video_codec")
    val audioCodec  = params.get("audio_codec")
    val width       = params.getAs[Int]("width")
    val height      = params.getAs[Int]("height")
    val frameRate   = params.getAs[Double]("frame_rate")
    val sizeBytes   = params.getAs[Long]("size_bytes")
    val sha512      = params.getAs[Array[Byte]]("sha512")
    controller
      .create(
        videoUUID,
        uri,
        container,
        videoCodec,
        audioCodec,
        width,
        height,
        frameRate,
        sizeBytes,
        description,
        sha512
      )
      .map(controller.toJson)
  }


  put("/:uuid") {
    validateRequest()
    val uuid = params
      .getAs[UUID]("uuid")
      .getOrElse(halt(BadRequest("""{error: "A 'uri' parameter is required"}""")))
    val videoUUID   = params.getAs[UUID]("video_uuid")
    val uri         = params.getAs[URI]("uri")
    val description = params.get("description")
    val container   = params.get("container")
    val videoCodec  = params.get("video_codec")
    val audioCodec  = params.get("audio_codec")
    val width       = params.getAs[Int]("width")
    val height      = params.getAs[Int]("height")
    val frameRate   = params.getAs[Double]("frame_rate")
    val sizeBytes   = params.getAs[Long]("size_bytes")
    val sha512      = params.getAs[Array[Byte]]("sha512")
    controller
      .update(
        uuid,
        videoUUID,
        uri,
        container,
        videoCodec,
        audioCodec,
        width,
        height,
        frameRate,
        sizeBytes,
        description,
        sha512
      )
      .map(controller.toJson)

  }

}
