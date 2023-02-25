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

package org.mbari.vampiresquid.api

import com.typesafe.config.ConfigFactory
import org.mbari.vampiresquid.auth.AuthorizationService
import org.scalatra.{ScalatraBase, Unauthorized}

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
    val clazz       = Class.forName(serviceName)
    clazz.getConstructor().newInstance().asInstanceOf[AuthorizationService]
  }
}
