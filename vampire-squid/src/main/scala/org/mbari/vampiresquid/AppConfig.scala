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

package org.mbari.vampiresquid

import scala.util.Try
import com.typesafe.config.ConfigFactory
import org.mbari.vampiresquid.etc.jwt.JwtService

object AppConfig:
  val Name: String = "vampire-squid"

  val Version: String = Try(getClass.getPackage.getImplementationVersion).getOrElse("0.0.0")

  val Description: String = "Video Asset Manager"

  private lazy val Config = ConfigFactory.load()

  lazy val JwtParameters: JwtParams =
    JwtParams(
      clientSecret = Config.getString("basicjwt.client.secret"),
      issuer = Config.getString("basicjwt.issuer"),
      signingSecret = Config.getString("basicjwt.signing.secret")
    )

  lazy val DefaultJwtService = new JwtService(
    issuer = JwtParameters.issuer,
    apiKey = JwtParameters.clientSecret,
    signingSecret = JwtParameters.signingSecret
  )

final case class JwtParams(
    clientSecret: String,
    issuer: String,
    signingSecret: String
)
