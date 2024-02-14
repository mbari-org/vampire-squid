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

package org.mbari.vampiresquid.repository.jpa

import org.mbari.vampiresquid.domain.{Media, Video, VideoReference, VideoSequence}
import org.junit.Assert.{assertArrayEquals, assertEquals, fail}

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

    def assertSameVideo(v0: Video, v1: Video): Unit =
        assertEquals(v0.description, v1.description)
        assertEquals(v0.duration, v1.duration)
        assertEquals(v0.name, v1.name)
        assertEquals(v0.start_timestamp, v1.start_timestamp)

    def assertSameVideoSequence(v0: VideoSequence, v1: VideoSequence): Unit =
        assertEquals(v0.name, v1.name)
        assertEquals(v0.description, v1.description)
        assertEquals(v0.camera_id, v1.camera_id)

    def assertSameVideoReference(v0: VideoReference, v1: VideoReference): Unit =
        assertEquals(v0.audio_codec, v1.audio_codec)
        assertEquals(v0.container, v1.container)
        assertEquals(v0.description, v1.description)
        assertEquals(v0.frame_rate, v1.frame_rate)
        assertEquals(v0.height, v1.height)
        assertEquals(v0.height, v1.height)
        if v0.sha512.isEmpty && v0.sha512.isEmpty then {
            // ok. do nothing
        }
        else if v0.sha512.nonEmpty && v0.sha512.nonEmpty then assertArrayEquals(v0.sha512.get, v1.sha512.get)
        else fail("Only one video reference has a checksum")
        assertEquals(v0.uri, v1.uri)
        assertEquals(v0.video_codec, v1.video_codec)
        assertEquals(v0.width, v1.width)

    def deepAssertSameVideo(v0: Video, v1: Video): Unit =
        assertSameVideo(v0, v1)
//        assert(v0.videoReferences.diff(v1.videoReferences).isEmpty)
        for w0 <- v0.videoReferences
        do
            v1.videoReferences.find(_.uuid == w0.uuid) match
                case None     => fail(s"VideoReference with uuid = ${w0.uuid} was not found in both videos")
                case Some(w1) => assertSameVideoReference(w0, w1)

    def deepAssertSameVideoSequence(v0: VideoSequence, v1: VideoSequence): Unit =
        assertSameVideoSequence(v0, v1)
//        assert(v0.videos.diff(v1.videos).isEmpty)
        for w0 <- v0.videos
        do
            v1.videos.find(_.uuid == w0.uuid) match
                case None     => fail(s"Video with uuid = ${w0.uuid} not found in both video sequences")
                case Some(w1) => deepAssertSameVideo(w0, w1)
