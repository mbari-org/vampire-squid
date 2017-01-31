package org.mbari.vars.vam.auth

import javax.servlet.http.HttpServletRequest

/**
 * AuthorizationServices are used to validate requests, especially those that
 * modify database content. These services do 2 things:
 * 1. Provide authentication credentials
 * 2. Validate those credentials.
 *
 *
 * @author Brian Schlining
 * @since 2017-01-18T16:31:00
 */
trait AuthorizationService {

  /**
   * Returns content to the client that can be used for validating subsequent
   * API requests. The exact mechanism is left to specific implementations.
   *
   * @param request The http request
   * @return validation credentialials. None is returned if the request is not
   *         valid.
   */
  def requestAuthorization(request: HttpServletRequest): Option[String]

  def validateAuthorization(request: HttpServletRequest): Boolean

}
