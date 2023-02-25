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

package org.mbari.vampiresquid.repository.jpa

import org.mbari.vampiresquid.Constants
import java.net.URI
import java.time.{Duration, Instant}
import org.slf4j.LoggerFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-17T16:00:00
  */
class GsonSpec extends AnyFlatSpec with Matchers {

  private[this] val log = LoggerFactory.getLogger(getClass)

  "GSON" should "serialize a VideoSequence" in {

    val videoSequence = VideoSequence("Foo", "Bar")
    val json          = Constants.GSON.toJson(videoSequence)
    log.info(json)

  }

  it should "serialize a VideoSequence with Videos" in {
    val videoSequence = VideoSequence(
      "Foo",
      "Bar",
      Seq(Video("bar1", Instant.now), Video("bar1", Instant.now, Duration.ofSeconds(23)))
    )

    val json = Constants.GSON.toJson(videoSequence)
    log.info(json)
  }

  it should "serialize a VideoSequence with Videos that have VideoReferences" in {
    val videoSequence = VideoSequence(
      "Foo",
      "Bar",
      Seq(
        Video(
          "bar1",
          Instant.now,
          videoReferences = Seq(
            VideoReference(new URI("uri:mbari:tape:T0123-04HD")),
            VideoReference(
              new URI("http://foo.bar/somevideo.mp4"),
              "video/mp4",
              "hevc",
              "pcm_s24le",
              1920,
              1080
            )
          )
        ),
        Video("bar2", Instant.now, Duration.ofSeconds(23))
      )
    )

    val json = Constants.GSON.toJson(videoSequence)
    log.info(json)
  }

}
