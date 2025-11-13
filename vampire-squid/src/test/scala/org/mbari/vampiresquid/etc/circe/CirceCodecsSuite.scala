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

package org.mbari.vampiresquid.etc.circe

import org.mbari.vampiresquid.domain.LastUpdatedTime
import CirceCodecs.{given, *}
import java.time.Instant
import org.mbari.vampiresquid.domain.Media
import java.util.HexFormat


class CirceCodecsSuite extends munit.FunSuite:

    test("stringify"):
        val lut = LastUpdatedTime(Instant.parse("2021-01-01T00:00:00Z"))
        val json = lut.stringify
        assert(json != null)
        assertEquals(json, """{"timestamp":"2021-01-01T00:00:00Z"}""")


    test("reify"):
        val json = """{"timestamp":"2021-01-01T00:00:00Z"}"""
        json.reify[LastUpdatedTime] match
            case Left(_) => fail("Failed to parse json")
            case Right(lut) =>
                assert(lut != null)
                assertEquals(lut.timestamp, Instant.parse("2021-01-01T00:00:00Z"))


    test("reify media w/ base64 sha512"):
        val json = """
        {
            "camera_id": "Doc Ricketts",
            "container": "video/quicktime",
            "duration_millis": 300050,
            "frame_rate": 59.94005994005994,
            "height": 2160,
            "sha512": "4mBrR7GSs3uUElY7vNOy077LG5INAmfd3CtY0/dLAKKxeS3YgnsKVE3tGR0yMqFZJQL/2dNRJtR3OFZIJxtLyA==",
            "size_bytes": 64185148974,
            "start_timestamp": "2025-11-05T23:15:08Z",
            "uri": "https://m3.shore.mbari.org/videos/M3/master/DocRicketts/2025/11/1472/D1472_20251105T231508Z_prores.mov",
            "video_codec": "prores",
            "video_name": "Doc Ricketts 1472 20251105T231508Z",
            "video_sequence_name": "Doc Ricketts 1472",
            "width": 3840
        }
        """
        json.reify[Media] match
            case Left(err) => fail(s"Failed to parse json: $err")
            case Right(media) =>
                assert(media != null)
                val expectedSha512 =
                    java.util.Base64.getDecoder.decode(
                        "4mBrR7GSs3uUElY7vNOy077LG5INAmfd3CtY0/dLAKKxeS3YgnsKVE3tGR0yMqFZJQL/2dNRJtR3OFZIJxtLyA=="
                    )
                assert(media.sha512.isDefined)
                assertEquals(media.sha512.get.toSeq, expectedSha512.toSeq)

    test("reify media w/ hex sha512"):
        val json = """
        {
            "camera_id": "Doc Ricketts",
            "container": "video/quicktime",
            "duration_millis": 300050,
            "frame_rate": 59.94005994005994,
            "height": 2160,
            "sha512": "49C64CFC24A17F06348A38E8BDFF1998FFD2C16ABE94FF8393F90C4ED629F6DA45289C7FAE2852F21A751A3A08927AD52CAB1FFE885A12845650ED83105A3C6C",
            "size_bytes": 64185148974,
            "start_timestamp": "2025-11-05T23:15:08Z",
            "uri": "https://m3.shore.mbari.org/videos/M3/master/DocRicketts/2025/11/1472/D1472_20251105T231508Z_prores.mov",
            "video_codec": "prores",
            "video_name": "Doc Ricketts 1472 20251105T231508Z",
            "video_sequence_name": "Doc Ricketts 1472",
            "width": 3840
        }
        """
        json.reify[Media] match
            case Left(err) => fail(s"Failed to parse json: $err")
            case Right(media) =>
                assert(media != null)
                val expectedSha512 = HexFormat.of().parseHex(
                        "49C64CFC24A17F06348A38E8BDFF1998FFD2C16ABE94FF8393F90C4ED629F6DA45289C7FAE2852F21A751A3A08927AD52CAB1FFE885A12845650ED83105A3C6C"
                    )
                assert(media.sha512.isDefined)
                assertEquals(media.sha512.get.toSeq, expectedSha512.toSeq)



