package org.mbari.vars.vam.dao.jpa

import java.time.{Duration, Instant}

import org.scalatest.{FlatSpec, Matchers}

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-17T16:00:00
  */
class GsonSpec extends FlatSpec with Matchers {

  "GSON" should "serialize a VideoSequence" in {

    val videoSequence = VideoSequence("Foo", "Bar")
    val json = Constants.GSON.toJson(videoSequence)
    println(json)

  }

  it should "serialize a VideoSequence with Videos" in {
    val videoSequence = VideoSequence("Foo", "Bar", Seq(
      Video("bar1", Instant.now),
      Video("bar1", Instant.now, Duration.ofSeconds(23))
    ))

    val json = Constants.GSON.toJson(videoSequence)
    println(json)
  }

}
