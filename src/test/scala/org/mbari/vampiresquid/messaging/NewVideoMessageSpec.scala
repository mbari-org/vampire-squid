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

import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.domain.{Video, VideoReference, VideoSequence}
import java.net.URI
import java.time.{Duration, Instant}
import java.util.UUID
import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mbari.vampiresquid.domain.Media2

/**
  * @author Brian Schlining
  * @since 2017-03-15T09:43:00
  */
class NewVideoMessageSpec extends AnyFlatSpec with Matchers with Inside {

  "NewVideoMessage" should "round-trip to/from JSON" in {

    val uri               = new URI("uri:mbari:tape:T0123")
    val container         = "video/mp4"
    val vcodec            = "h264"
    val acodec            = "aac"
    val width             = 1920
    val height            = 1080
    val videoName         = getClass.getSimpleName
    val start             = Instant.now
    val duration          = Duration.ofMinutes(45)
    val videoSequenceName = "Foo"
    val videoSequenceUuid = UUID.randomUUID()
    val cameraId          = "Ventana"

    val videoReference = VideoReference(UUID.randomUUID, 
      uri = uri, 
      container = Some(container), 
      video_codec = Some(vcodec), 
      audio_codec = Some(acodec), 
      width = Some(width), 
      height = Some(height))
    val video          = Video(UUID.randomUUID, videoName, start, Some(duration.toMillis()), video_references = List(videoReference))
    val videoSequence  = VideoSequence(UUID.randomUUID, videoSequenceName, cameraId, videos = List(video))
    val vs = videoSequence.copy(uuid = videoSequenceUuid)
    val msg = NewVideoMessage(Media2.from(vs, videoReference))
    // println(msg)
    val json = Constants.GSON.toJson(msg)
    val obj  = Constants.GSON.fromJson(json, classOf[NewVideoMessage])
    obj should not be null
    inside(obj) {
      case NewVideoMessage(
          vsUuid,
          vsName,
          camId,
          videoUuid,
          vName,
          startTimestamp,
          durationMillis,
          vr
          ) =>
        vsUuid should be(videoSequenceUuid)
        vsName should be(videoSequenceName)
        camId should be(cameraId)
        videoUuid should be(null)
        vName should be(videoName)
        startTimestamp should be(start)
        durationMillis should be(duration)
        vr should be(uri)
        // vr.container should be(container)
        // vr.videoCodec should be(vcodec)
        // vr.audioCodec should be(acodec)
        // vr.width should be(width)
        // vr.height should be(height)

    }
  }

}
