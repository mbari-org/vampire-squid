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

import com.google.gson.annotations.Expose
import org.mbari.vampiresquid.etc.jpa.UUIDConverter
import org.mbari.vampiresquid.repository.PersistentObject

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID
import javax.persistence.{Column, Convert, GeneratedValue, Id, MappedSuperclass, Version}

@MappedSuperclass
abstract class BaseEntity extends PersistentObject {

  @Expose(serialize = true)
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "uuid", nullable = false, updatable = false, length = 36)
  @Convert(converter = classOf[UUIDConverter])
  var uuid: UUID = _

  def primaryKey: Option[UUID] = Option(uuid)

  @Expose(serialize = true)
  @Column(name = "description", length = 2048)
  var description: String = _

  /** Optimistic lock to prevent concurrent overwrites */
  @Expose(serialize = true)
  @Version
  @Column(name = "last_updated_time")
  protected var lastUpdatedTime: Timestamp = _

  def lastUpdated: Option[Instant] = Option(lastUpdatedTime).map(_.toInstant)


}
