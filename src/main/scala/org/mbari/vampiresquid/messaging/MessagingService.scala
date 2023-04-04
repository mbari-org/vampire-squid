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

package org.mbari.vampiresquid.messaging


import scala.concurrent.ExecutionContext
import org.mbari.vampiresquid.domain.{Media2, VideoReference}

/**
  * @author Brian Schlining
  * @since 2017-03-13T16:40:00
  */
trait MessagingService {

  /**
    * When a new videoReference is registered it gets passed to this method which will
    * post a notification about it to whatever message broker that you implement.
    *
    * @param videoReference
    */
  def newVideoReference(media: Media2)(implicit ec: ExecutionContext): Unit

}
