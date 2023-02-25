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

package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-23T13:51:00
  */
trait BaseController {

  private[this] val gson = Constants.GSON

  def daoFactory: JPADAOFactory
  def toJson(obj: Any): String                         = gson.toJson(obj)
  def fromJson[T](json: String, classOfT: Class[T]): T = gson.fromJson(json, classOfT)

}
