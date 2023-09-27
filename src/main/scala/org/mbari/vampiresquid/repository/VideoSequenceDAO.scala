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

package org.mbari.vampiresquid.repository

import org.mbari.vampiresquid.repository.jpa.entity.IPersistentObject

import java.time.{Duration, Instant}
import java.util.UUID

/** Defines the API methods used for accessing VideoSequence objects
  *
  * @author
  *   Brian Schlining
  * @since 2016-05-05T12:54:00
  */
trait VideoSequenceDAO[T <: IPersistentObject] extends DAO[T]:

  def findByName(name: String): Option[T]
  def findByCameraID(cameraID: String): Iterable[T]
  def findByVideoUUID(uuid: UUID): Option[T]
  def findByTimestamp(timestamp: Instant, window: Duration): Iterable[T]
  def findByNameAndTimestamp(name: String, timestamp: Instant, window: Duration): Iterable[T]
  def findByCameraIDAndTimestamp(
      cameraID: String,
      timestamp: Instant,
      window: Duration
  ): Iterable[T]
  def findAllNames(): Iterable[String]
  def findAllCameraIDs(): Iterable[String]
  def findAllNamesByCameraID(cameraID: String): Iterable[String]
