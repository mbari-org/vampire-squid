package org.mbari.vars.vam.auth

import com.auth0.jwt.JWTSigner
import com.auth0.jwt.JWTVerifier
import org.mbari.vars.vam.Constants

import scala.collection.JavaConverters._
import java.lang.{Long => JLong}
import java.time.Instant

import scala.util.control.NonFatal

/**
 * @author Brian Schlining
 * @since 2016-12-29T16:11:00
 */
class BasicJWTService extends AuthorizationService {

  private[this] val issuer = Constants.CONFIG.getString("basicjwt.issuer")
  private[this] val apiKey = Constants.CONFIG.getString("basicjwt.client.secret")
  private[this] val signingSecret = Constants.CONFIG.getString("basicjwt.signing.secret")

  private[this] val verifier = new JWTVerifier(signingSecret)

  override def authorize(token: String): Option[Authorization] =
    if (apiKey.equals(token)) Some(Authorization("BEARER", newAccessToken))
    else None

  override def verify(authorization: Authorization): Boolean = {
    var ok = false
    if (authorization.tokenType.equalsIgnoreCase("BEARER")) {
      try {
        val claims = verifier.verify(authorization.accessToken)
        val exp = claims.getOrDefault("exp", 0)
        val expiration = Instant.ofEpochSecond(exp)
        val now = Instant.now()
        ok = expiration.isAfter(now)
      }
      catch {
        case NonFatal(e) => // Do nothing
      }
    }
    ok
  }

  private def newAccessToken: String = {

    val iat: JLong = System.currentTimeMillis() / 1000L // issued at claim
    val exp: JLong = iat + 86400L // expires claim. In this case the token expires in 24 hours

    val signer = new JWTSigner(signingSecret)
    val claims = new java.util.HashMap[String, Object]()
    claims.put("iss", issuer)
    claims.put("exp", exp)
    claims.put("iat", iat)

    return signer.sign(claims)
  }
}
