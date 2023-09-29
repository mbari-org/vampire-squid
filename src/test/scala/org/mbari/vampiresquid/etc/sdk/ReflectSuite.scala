/*
 * Copyright 2021 MBARI
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

package org.mbari.vampiresquid.etc.sdk

import org.mbari.vampiresquid.repository.jpa.TestUtils
import org.mbari.vampiresquid.domain.Media


class ReflectSuite extends munit.FunSuite:

  test("toFormMap"):
    val vs = TestUtils.build(1, 1, 1).head
    val media = Media.from(vs.getVideoReferences().get(0))
    val map = Reflect.toFormMap(media)
    assertEquals(map("video_sequence_name"), media.video_sequence_name.get)
    assertEquals(map("video_name"), media.video_name.get)
    assertEquals(map("camera_id"), media.camera_id.get)
    assertEquals(map("uri"), media.uri.get.toString)
    assertEquals(map("start_timestamp"), media.start_timestamp.get.toString)
    assertEquals(map("duration_millis"), media.duration_millis.get.toString)
    assertEquals(map("container"), media.container.get)
    assertEquals(map("video_codec"), media.video_codec.get)
    assertEquals(map("audio_codec"), media.audio_codec.get)
    assertEquals(map("width"), media.width.get.toString)
    assertEquals(map("height"), media.height.get.toString)
    assertEquals(map("frame_rate"), media.frame_rate.get.toString)
    assertEquals(map("size_bytes"), media.size_bytes.get.toString)
    assertEquals(map("description"), media.description.get)
    // assertEquals(map("sha512"), media.sha512.get.toString)
    assertEquals(map("video_description"), media.video_description.get)
    assertEquals(map("video_sequence_description"), media.video_sequence_description.get)






  
