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

import org.mbari.vampiresquid.etc.sdk.{FormTransform, ToStringTransforms}
import org.mbari.vampiresquid.etc.sdk.ToStringTransforms.transform
import org.mbari.vampiresquid.repository.jpa.entity.VideoSequenceEntity

import java.time.Instant
import java.util.UUID
import scala.jdk.CollectionConverters.*

case class VideoSequenceCreate(name: String, camera_id: String, description: Option[String] = None)

case class VideoSequenceUpdate(
    name: Option[String] = None,
    camera_id: Option[String] = None,
    description: Option[String] = None
)

case class VideoSequence(
    uuid: UUID,
    name: String,
    camera_id: String,
    description: Option[String] = None,
    last_updated_time: Option[Instant] = None,
    videos: List[Video] = Nil
):

    def cameraId: String                           = camera_id
    def lastUpdatedTimestamp: Option[Instant]      = last_updated_time
    lazy val videoReferences: List[VideoReference] = videos.flatMap(_.video_references)

object VideoSequence:

    def from(v: VideoSequenceEntity): VideoSequence =
        VideoSequence(
            v.getUuid,
            v.getName,
            v.getCameraID,
            Option(v.getDescription),
            Option(v.getLastUpdatedTime).map(_.toInstant),
            v.getVideos.asScala.map(Video.from).toList
        )

    def toFormMap(v: VideoSequence): String =
        s"uuid=${v.uuid}&name=${v.name}&camera_id=${v.camera_id}" +
            v.description.map(s => s"&description=$s").getOrElse("")
