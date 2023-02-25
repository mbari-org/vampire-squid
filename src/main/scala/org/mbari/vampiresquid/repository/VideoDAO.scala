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

package org.mbari.vampiresquid.repository

import java.time.{Duration, Instant}
import java.util.UUID

/**
  * Defines the API methods used for accessing Video objects
  *
  * @author Brian Schlining
  * @since 2016-05-05T12:59:00
  */
trait VideoDAO[T <: PersistentObject] extends DAO[T] {

  def findByTimestamp(start: Instant, window: Duration): Iterable[T]
  def findByName(name: String): Option[T]
  def findByVideoSequenceUUID(uuid: UUID): Iterable[T]
  def findByVideoReferenceUUID(uuid: UUID): Option[T]
  def findAllNames(): Iterable[String]
  def findAllNamesAndTimestamps(): Iterable[(String, Instant)]

  /**
    * Finds videos with a start date between the 2 timestamps
    * @param t0
    * @param t1
    * @return
    */
  def findBetweenTimestamps(t0: Instant, t1: Instant): Iterable[T]

  /**
    * Get the names of the videos belonging to a particular video sequence
    * @param videoSequenceName
    * @return
    */
  def findNamesByVideoSequenceName(videoSequenceName: String): Iterable[String]

}
