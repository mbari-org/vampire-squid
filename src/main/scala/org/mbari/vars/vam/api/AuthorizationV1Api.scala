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

package org.mbari.vars.vam.api

import org.scalatra.Unauthorized
import org.scalatra.swagger.Swagger

import scala.concurrent.ExecutionContext

/**
  * @author Brian Schlining
  * @since 2017-02-06T08:42:00
  */
class AuthorizationV1Api(implicit val swagger: Swagger, val executor: ExecutionContext)
    extends APIStack {

  override protected def applicationDescription: String = "Authorization API (v1)"

  before() {
    contentType = "application/json"
  }

  post("/") {
    authorizationService.requestAuthorization(request) match {
      case None    => halt(Unauthorized())
      case Some(s) => s
    }
  }

}
