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



