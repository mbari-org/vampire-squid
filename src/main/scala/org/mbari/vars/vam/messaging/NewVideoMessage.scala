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

package org.mbari.vars.vam.messaging

import com.google.gson.annotations.Expose
import java.util.UUID
import scala.annotation.meta.field
import java.time.Instant
import java.time.Duration
import org.mbari.vars.vam.dao.jpa.VideoReferenceEntity

case class NewVideoMessage(
    @(Expose @field)(serialize = true) videoSequenceUuid: UUID,
    @(Expose @field)(serialize = true) videoSequenceName: String,
    @(Expose @field)(serialize = true) cameraId: String,
    @(Expose @field)(serialize = true) videoUuid: UUID,
    @(Expose @field)(serialize = true) videoName: String,
    @(Expose @field)(serialize = true) startTimestamp: Instant,
    @(Expose @field)(serialize = true) durationMillis: Duration,
    @(Expose @field)(serialize = true) videoReference: VideoReferenceEntity
)

object NewVideoMessage {
  def apply(videoReference: VideoReferenceEntity): NewVideoMessage = {
    val v  = videoReference.video
    val vs = v.videoSequence
    NewVideoMessage(
      vs.uuid,
      vs.name,
      vs.cameraID,
      v.uuid,
      v.name,
      v.start,
      v.duration,
      videoReference
    )
  }
}