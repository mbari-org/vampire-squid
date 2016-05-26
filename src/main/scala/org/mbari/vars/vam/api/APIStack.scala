package org.mbari.vars.vam.api

import java.time.Instant
import java.util.UUID

import org.scalatra.{ ContentEncodingSupport, FutureSupport, ScalatraServlet }
import org.scalatra.swagger.SwaggerSupport
import org.scalatra.util.conversion.TypeConverter

import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-23T13:32:00
 */
abstract class APIStack extends ScalatraServlet
    with ContentEncodingSupport
    with SwaggerSupport
    with FutureSupport {

  implicit val stringToUUID = new TypeConverter[String, UUID] {
    override def apply(s: String): Option[UUID] = Try(UUID.fromString(s)).toOption
  }

  implicit val stringToInstant = new TypeConverter[String, Instant] {
    override def apply(s: String): Option[Instant] = Try(Instant.parse(s)).toOption
  }

}
