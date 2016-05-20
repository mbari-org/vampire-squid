package org.mbari.vars.vam.api

import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{Swagger, SwaggerSupport}
import org.scalatra.swagger._
import org.slf4j.LoggerFactory
import org.scalatra.{ Ok, NotFound, ScalatraServlet }

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-20T14:45:00
  */
class VideoSequenceV1Api(implicit val swagger: Swagger) extends ScalatraServlet
    with SwaggerSupport {

  private[this] val log = LoggerFactory.getLogger(getClass)

  override protected def applicationDescription: String = "Video Sequence API (v1)"

  override protected val applicationName: Option[String] = Some("VideoSequenceAPI")

  before() {
    contentType = formats("json")
    response.headers += ("Access-Control-Allow-Origin" -> "*")
  }


}
