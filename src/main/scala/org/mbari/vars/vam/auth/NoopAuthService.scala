package org.mbari.vars.vam.auth

import javax.servlet.http.HttpServletRequest

/**
 * Service that does not validation. All requests are valid. Useful for testing
 *
 * @author Brian Schlining
 * @since 2017-01-19T08:50:00
 */
class NoopAuthService extends AuthorizationService {

  override def requestAuthorization(request: HttpServletRequest): Option[String] = None

  override def validateAuthorization(request: HttpServletRequest): Boolean = true
}
