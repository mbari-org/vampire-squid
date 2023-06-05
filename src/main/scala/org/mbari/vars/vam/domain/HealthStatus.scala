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

import org.mbari.vars.vam.AppConfig



final case class HealthStatus(
    jdkVersion: String,
    availableProcessors: Int,
    freeMemory: Long,
    maxMemory: Long,
    totalMemory: Long,
    application: String = AppConfig.Name,
    version: String = AppConfig.Version,
    description: String = AppConfig.Description
)

object HealthStatus:

  /**
   * @return
   *   a HealthStatus object with the current JVM stats
   */
  def default: HealthStatus =
    val runtime = Runtime.getRuntime
    HealthStatus(
      jdkVersion = Runtime.version.toString,
      availableProcessors = runtime.availableProcessors,
      freeMemory = runtime.freeMemory,
      maxMemory = runtime.maxMemory,
      totalMemory = runtime.totalMemory
    )

  def empty(application: String): HealthStatus =
    HealthStatus(
      jdkVersion = "",
      availableProcessors = 0,
      freeMemory = 0,
      maxMemory = 0,
      totalMemory = 0,
      application = application,
      version = "0.0.0",
      description = ""
    )

