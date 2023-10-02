/*
 * Copyright 2021 MBARI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vampiresquid.etc.jwt

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.JWT
import com.typesafe.config.ConfigFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import scala.util.control.NonFatal
import org.mbari.vampiresquid.JwtParams
import org.mbari.vampiresquid.domain.Authorization

/** To use this authentication. The client and server should both have a shared secret (aka client secret). The client sends this to the
  * server in a authorization header. If the secret is correct, the server will send back a JWT token that can be used to validate
  * subsequent requests.
  *
  * {{{
  *   Client                                                                Server
  *     |-------> POST /auth: Authorization: APIKEY <client_secret>      ----->|
  *     |                                                                      |
  *     |<------- {'access_token': <token>, 'token_type': 'Bearer'}     <------|
  *     |                                                                      |
  *     |                                                                      |
  *     |-------> POST /somemethod: Authorization: Bearer <token>       ------>|
  *     |                                                                      |
  *     |<------- 200                                                   <------|
  * }}}
  * @author
  *   Brian Schlining
  * @since 2017-01-18T16:42:00
  */
class BasicJwtService(apiKey: String, issuer: String, signingSecret: String):

  def this(params: JwtParams) = this(params.clientSecret, params.issuer, params.signingSecret)

  private[this] val algorithm = Algorithm.HMAC512(signingSecret)

  private[this] val verifier = JWT
    .require(algorithm)
    .withIssuer(issuer)
    .build()

  private def isValid(auth: Option[Authorization]): Boolean =
    try
      auth match
        case None    => false
        case Some(a) =>
          if (a.tokenType.equalsIgnoreCase("BEARER"))
            val _ = verifier.verify(a.accessToken)
            true
          else false
    catch case NonFatal(e) => false

  private def parseAuthHeader(header: String): Authorization =
    val parts       = header.split("\\s")
    val tokenType   = if (parts.length == 1) "undefined" else parts(0)
    val accessToken = if (parts.length == 1) parts(0) else parts(1)
    Authorization(tokenType, accessToken)

  def validate(auth: Authorization): Boolean = isValid(Some(auth))

  def requestAuthorization(providedApiKey: String): Option[Authorization] =
    authorize(providedApiKey).map(jwt => Authorization("Bearer", jwt))

  def authorize(providedApiKey: String): Option[String] =
    if (apiKey == providedApiKey)
      val now      = Instant.now()
      val tomorrow = now.plus(1, ChronoUnit.DAYS)
      val iat      = Date.from(now)
      val exp      = Date.from(tomorrow)

      val jwt = JWT
        .create()
        .withIssuer(issuer)
        .withIssuedAt(iat)
        .withExpiresAt(exp)
        .sign(algorithm)

      Some(jwt)
    else None
