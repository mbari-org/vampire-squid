package org.mbari.vars.vam.api

import java.net.URI
import java.time.format.DateTimeFormatter
import java.time.{Duration, Instant}
import java.util.UUID
import javax.servlet.http.HttpServletRequest

import org.mbari.vars.vam.dao.jpa.ByteArrayConverter
import org.scalatra.{ContentEncodingSupport, FutureSupport, NotFound, ScalatraServlet}
import org.scalatra.swagger.SwaggerSupport
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
abstract class APIStack extends ScalatraServlet
    with ApiAuthenticationSupport
    with ContentEncodingSupport
    with SwaggerSupport
    with FutureSupport {

  before() {
    contentType = "application/json"
    response.headers += ("Access-Control-Allow-Origin" -> "*")
  }

  protected[this] val timeFormatter = DateTimeFormatter.ISO_DATE_TIME

  implicit val stringToUUID = new TypeConverter[String, UUID] {
    override def apply(s: String): Option[UUID] = Try(UUID.fromString(s)).toOption
  }

  implicit val stringToInstant = new TypeConverter[String, Instant] {
    override def apply(s: String): Option[Instant] = {
      //Try(Instant.parse(s)).toOption
      Try(Instant.from(timeFormatter.parse(s))).toOption
    }
  }

  implicit val stringToDuration = new TypeConverter[String, Duration] {
    override def apply(s: String): Option[Duration] = Try(Duration.ofMillis(s.toLong)).toOption
  }

  implicit val stringToURI = new TypeConverter[String, URI] {
    override def apply(s: String): Option[URI] = Try(URI.create(s)).toOption
  }

  implicit val stringToByteArray = new TypeConverter[String, Array[Byte]] {
    override def apply(s: String): Option[Array[Byte]] = Try(ByteArrayConverter.decode(s)).toOption
  }

  /**
   * Parse a form post into key value pairs. e.g.
   * Transform "parameter=value&also=another" to Map("parameter" -> "value", "also" -> "another")
   * @param body
   * @return
   */
  def parsePostBody(body: String): Seq[(String, String)] = body.split('&')
    .map(p => p.split('='))
    .filter(_.size == 2)
    .map(a => a(0) -> a(1))

  /**
   * Read
   * @param request
   * @return
   */
  def readBody(request: HttpServletRequest): String = {
    try {
      val reader = request.getInputStream
      val body = Source.fromInputStream(reader, "UTF-8")
        .getLines()
        .mkString("\n")
      reader.close()
      body
    } catch {
      case NonFatal(e) => ""
    }
  }

}
