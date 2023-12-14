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

package org.mbari.vampiresquid.etc.circe

import org.mbari.vampiresquid.domain.LastUpdatedTime
import CirceCodecs.{given, *}
import java.time.Instant

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



