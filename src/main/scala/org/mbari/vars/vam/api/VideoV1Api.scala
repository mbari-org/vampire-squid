package org.mbari.vars.vam.api

import org.scalatra.swagger.Swagger

import scala.concurrent.ExecutionContext

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-24T13:41:00
 */
class VideoV1Api(implicit val swagger: Swagger, val executor: ExecutionContext)
    extends APIStack {
  override protected def applicationDescription: String = "TODO"
}
