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
import java.time.format.DateTimeFormatter
import java.time.{Duration, Instant}
import java.util.UUID
import javax.servlet.http.HttpServletRequest

import org.mbari.vars.vam.dao.jpa.ByteArrayConverter
import org.scalatra.{ContentEncodingSupport, FutureSupport, ScalatraServlet}
// import org.scalatra.swagger.SwaggerSupport
import org.scalatra.util.conversion.TypeConverter

import scala.io.Source
import scala.util.Try
import scala.util.control.NonFatal

/**
  * All Api classes should mixin this trait. It defines the common traits used by all implementations
  * as well implicits need for type conversions.
  *
  * @author Brian Schlining
  * @since 2016-05-23T13:32:00
  */
abstract class APIStack
    extends ScalatraServlet
    with ApiAuthenticationSupport
    with ContentEncodingSupport
    with FutureSupport {

  before() {
    contentType = "application/json"
  }

  protected[this] val timeFormatter        = DateTimeFormatter.ISO_DATE_TIME
  protected[this] val compactTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")
  protected[this] val compactTimeFormatter1 = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX")
  protected[this] val compactTimeFormatter2 = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSSSSX")

  implicit val stringToUUID: TypeConverter[String, UUID] = new TypeConverter[String, UUID] {
    override def apply(s: String): Option[UUID] =
      Try(UUID.fromString(s)).toOption
  }

  implicit protected val stringToInstant: TypeConverter[String, Instant] = new TypeConverter[String, Instant] {
    override def apply(s: String): Option[Instant] = {
      val try1 = Try(Instant.from(compactTimeFormatter.parse(s))).toOption
      val try2 = try1 match {
        case Some(_) => try1
        case None => Try(Instant.from(timeFormatter.parse(s))).toOption
      }
      val try3 = try2 match {
        case Some(_) => try2
        case None => Try(Instant.from(compactTimeFormatter1.parse(s))).toOption
      }
      val try4 = try3 match {
        case Some(_) => try3
        case None => Try(Instant.from(compactTimeFormatter2.parse(s))).toOption
      }
      try4
    }
  }

  implicit val stringToDuration: TypeConverter[String, Duration] = new TypeConverter[String, Duration] {
    override def apply(s: String): Option[Duration] =
      Try(Duration.ofMillis(s.toLong)).toOption
  }

  implicit val stringToURI: TypeConverter[String, URI] = new TypeConverter[String, URI] {
    override def apply(s: String): Option[URI] = Try(URI.create(s)).toOption
  }

  implicit val stringToByteArray: TypeConverter[String, Array[Byte]] = new TypeConverter[String, Array[Byte]] {
    override def apply(s: String): Option[Array[Byte]] =
      Try(ByteArrayConverter.decode(s)).toOption
  }

  /**
    * Parse a form post into key value pairs. e.g.
    * Transform "parameter=value&also=another" to Map("parameter" -> "value", "also" -> "another")
    * @param body
    * @return
    */
  def parsePostBody(body: String): Seq[(String, String)] =
    body
      .split('&')
      .toIndexedSeq
      .map(p => p.split('='))
      .filter(_.length == 2)
      .map(a => a(0) -> a(1))

  /**
    * Read
    * @param request
    * @return
    */
  def readBody(request: HttpServletRequest): String = {
    try {
      val reader = request.getInputStream
      val body = Source
        .fromInputStream(reader, "UTF-8")
        .getLines()
        .mkString("\n")
      reader.close()
      body
    }
    catch {
      case NonFatal(e) => ""
    }
  }

}
