package org.mbari.vars.vam.api

import org.scalatra.ScalatraServlet
import org.scalatra.swagger.{ JacksonSwaggerBase, Swagger }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-23T16:41:00
 */
class VAMSwagger(implicit val swagger: Swagger)
    extends ScalatraServlet with JacksonSwaggerBase {

}
