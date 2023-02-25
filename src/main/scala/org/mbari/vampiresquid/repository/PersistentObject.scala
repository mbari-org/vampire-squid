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

import java.util.UUID

/**
  * We have made a design decision to always use UUIDs (aka GUIDs) as the primary key. This requirement
  * is enforced so that external applications can rely on the use of that key. All persistent objects
  * should implement this trait
  *
  * @author Brian Schlining
  * @since 2016-05-05T16:21:00
  */
trait PersistentObject {

  def primaryKey: Option[UUID]
}
