package org.mbari.vars.vam.auth

/**
 * @author Brian Schlining
 * @since 2016-12-29T16:05:00
 */
trait AuthorizationService {

  def authorize(token: String): Option[Authorization]

  def verify(authorization: Authorization): Boolean

}
