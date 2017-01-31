package org.mbari.vars.vam.api

import com.typesafe.config.ConfigFactory
import org.mbari.vars.vam.auth.AuthorizationService
import org.scalatra.{ ScalatraBase, Unauthorized }

/**
 * @author Brian Schlining
 * @since 2017-01-18T16:27:00
 */
trait ApiAuthenticationSupport { self: ScalatraBase =>

  val authorizationService: AuthorizationService = ApiAuthenticationSupport.authorizationService

  protected def validateRequest(): Unit = {
    //println("VALIDATING: " + request)
    if (!authorizationService.validateAuthorization(request)) {
      halt(Unauthorized("The request did not include valid authorization credentials"))
    }
  }

}

object ApiAuthenticationSupport {

  private[this] val appConfig = ConfigFactory.load()

  def authorizationService: AuthorizationService = {
    val serviceName = appConfig.getString("authentication.service")
    val clazz = Class.forName(serviceName)
    clazz.newInstance().asInstanceOf[AuthorizationService]
  }
}
