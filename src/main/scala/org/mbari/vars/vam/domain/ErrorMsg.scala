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

sealed trait ErrorMsg:
  def message: String
  def responseCode: Int

/**
 * Just a simple class used to return a JSON error response
 * @param message
 *   the error message
 * @param responseCode
 *   the HTTP response code
 * @author
 *   Brian Schlining
 * @since 2021-11-23T11:00:00
 */
final case class StatusMsg(message: String, responseCode: Int)          extends ErrorMsg
final case class NotFound(message: String, responseCode: Int = 404)     extends ErrorMsg
final case class ServerError(message: String, responseCode: Int = 500)  extends ErrorMsg
final case class Unauthorized(message: String, responseCode: Int = 401) extends ErrorMsg
final case class BadRequest(message: String, responseCode: Int = 400)   extends ErrorMsg

