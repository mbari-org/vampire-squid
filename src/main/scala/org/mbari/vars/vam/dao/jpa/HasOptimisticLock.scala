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

package org.mbari.vars.vam.dao.jpa

import java.sql.Timestamp
import java.time.Instant
import javax.persistence.{Column, Version}

import com.google.gson.annotations.Expose

/**
  * OUr Entities should use optimixtic locks. This trait allows you to mixin the lock.
  *
  * @author Brian Schlining
  * @since 2016-05-05T16:22:00
  */
trait HasOptimisticLock {

  /** Optimistic lock to prevent concurrent overwrites */
  @Expose(serialize = true)
  @Version
  @Column(name = "last_updated_time")
  protected var lastUpdatedTime: Timestamp = _

  def lastUpdated: Option[Instant] = Option(lastUpdatedTime).map(_.toInstant)

}
