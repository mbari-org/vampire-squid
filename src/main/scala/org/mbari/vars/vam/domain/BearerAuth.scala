/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
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

package org.mbari.vars.vam.domain

/**
 * An authorization token and it's type. Typically the token is a JWT and the tokenType is "Bearer"
 * @param tokenType
 *   the type of the token ("Bearer")
 * @param token
 *   The JWT token
 * @author
 *   Brian Schlining
 * @since 2020-01-28T11:00:00
 */
case class BearerAuth(accessToken: String) extends Auth(BearerAuth.TokenType)

object BearerAuth:

  val TokenType = "Bearer"

  /**
   * Parse the value portion of an Authorization header into an Authorization object
   * @param authorization
   *   The value portion of an Authorization header
   * @return
   *   An Authorization object, None if it's not parseable
   */
  def parse(authorization: String): Option[BearerAuth] =
    val parts = authorization.split("\\s+")
    if (parts.length == 2 && parts(0).toLowerCase == TokenType.toLowerCase)
      Some(BearerAuth(parts(1)))
    else
      None

  val Invalid: BearerAuth = BearerAuth("")

