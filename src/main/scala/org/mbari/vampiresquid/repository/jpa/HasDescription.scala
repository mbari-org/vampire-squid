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

package org.mbari.vampiresquid.repository.jpa

import javax.persistence.Column

import com.google.gson.annotations.Expose

/**
  * Mixin for PersistentObjects that include a description field. A description is
  * essentially free text.
  *
  * @author Brian Schlining
  * @since 2016-06-02T14:53:00
  */
trait HasDescription {

  @Expose(serialize = true)
  @Column(name = "description", length = 2048)
  var description: String = _

}
