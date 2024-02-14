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

import org.mbari.vampiresquid.repository.jpa.entity.VideoEntity

import java.time.Instant
import java.util.UUID
import scala.jdk.CollectionConverters.*
import java.time.Duration

case class Video(
    uuid: UUID,
    name: String,
    start_timestamp: Instant,
    duration_millis: Option[Long] = None,
    description: Option[String] = None,
    last_updated_time: Option[Instant] = None,
    video_references: List[VideoReference] = Nil
):
    def lastUpdatedTimestamp: Option[Instant] = last_updated_time
    def start: Instant                        = start_timestamp
    lazy val duration: Option[Duration]       = duration_millis.map(Duration.ofMillis)
    def videoReferences: List[VideoReference] = video_references

object Video:

    def from(v: VideoEntity): Video =
        Video(
            v.getUuid,
            v.getName,
            v.getStart,
            Option(v.getDuration).map(_.toMillis),
            Option(v.getDescription),
            Option(v.getLastUpdatedTime).map(_.toInstant),
            v.getVideoReferences.asScala.map(VideoReference.from).toList
        )
