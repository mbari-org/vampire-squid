/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
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

import java.time.Instant
import java.util.UUID
import java.time.Duration

case class VideoUpdate(
    name: Option[String] = None,
    video_sequence_uuid: Option[UUID] = None,
    start: Option[Instant] = None,
    start_timestamp: Option[Instant] = None,
    duration_millis: Option[Long] = None,
    description: Option[String] = None
):

    def startTimestamp: Option[Instant] = start.orElse(start_timestamp)
    def duration: Option[Duration] = duration_millis.map(Duration.ofMillis)
