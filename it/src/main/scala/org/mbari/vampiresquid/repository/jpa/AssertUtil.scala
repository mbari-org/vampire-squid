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

package org.mbari.vampiresquid.repository.jpa

import org.mbari.vampiresquid.domain.Media
import org.junit.Assert.{assertArrayEquals, assertEquals}

object AssertUtil:
  
  def assertSameMedia(m0: Media, m1: Media): Unit =
    assertArrayEquals(m0.sha512.orNull, m1.sha512.orNull)
    assertEquals(m0.video_sequence_name, m1.video_sequence_name)
    assertEquals(m0.video_name, m1.video_name)
    assertEquals(m0.camera_id, m1.camera_id)
    assertEquals(m0.start_timestamp, m1.start_timestamp)
    assertEquals(m0.duration, m1.duration)
    assertEquals(m0.uri, m1.uri)
    assertEquals(m0.container, m1.container)
    assertEquals(m0.video_codec, m1.video_codec)
    assertEquals(m0.audio_codec, m1.audio_codec)
    assertEquals(m0.width, m1.width)
    assertEquals(m0.height, m1.height)
    assertEquals(m0.frame_rate, m1.frame_rate)
    assertEquals(m0.size_bytes, m1.size_bytes)
    assertEquals(m0.description, m1.description)
    assertEquals(m0.video_description, m1.video_description)
    assertEquals(m0.video_sequence_description, m1.video_sequence_description)

