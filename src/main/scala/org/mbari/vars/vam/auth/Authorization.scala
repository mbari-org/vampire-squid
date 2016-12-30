package org.mbari.vars.vam.auth

/**
 * @author Brian Schlining
 * @since 2016-12-29T16:05:00
 */
case class Authorization(tokenType: String, accessToken: String)

object Authorization {

  val TOKEN_TYPE_UNDEFINED = "undefined"

  def parseHttpHeader(auth: String): Unit = {
    val parts = auth.split("\\s")
    val (tokenType, accessToken) = if (parts.length == 1) (TOKEN_TYPE_UNDEFINED, parts(0))
    else (parts(0), parts(1))
    Authorization(tokenType, accessToken)
  }

}