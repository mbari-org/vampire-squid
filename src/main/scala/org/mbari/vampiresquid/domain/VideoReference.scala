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

package org.mbari.vampiresquid.domain

import java.net.URI
import java.time.Instant
import java.util.UUID

case class VideoReference(
                         uuid: UUID,
                         uri: URI,
                         container: Option[String] = None,
                         video_codec: Option[String] = None,
                         audio_codec: Option[String] = None,
                         width: Option[Int] = None,
                         height: Option[Int] = None,
                         frame_rate: Option[Double] = None,
                         size_bytes: Option[Long] = None,
                         sha512: Option[Array[Byte]] = None,
                         description: Option[String] = None,
                         last_updated_time: Option[Instant] = None
                         )
