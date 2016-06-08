package org.mbari.vars.vam.dao.jpa

import java.net.URI
import java.time.{ Duration, Instant }

import org.mbari.vars.vam.Constants
import org.scalatest.{ FlatSpec, Matchers }
import org.slf4j.LoggerFactory

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-17T16:00:00
 */
class GsonSpec extends FlatSpec with Matchers {

  private[this] val log = LoggerFactory.getLogger(getClass)

  "GSON" should "serialize a VideoSequence" in {

    val videoSequence = VideoSequence("Foo", "Bar")
    val json = Constants.GSON.toJson(videoSequence)
    log.info(json)

  }

  it should "serialize a VideoSequence with Videos" in {
    val videoSequence = VideoSequence("Foo", "Bar", Seq(
      Video("bar1", Instant.now),
      Video("bar1", Instant.now, Duration.ofSeconds(23))))

    val json = Constants.GSON.toJson(videoSequence)
    log.info(json)
  }

  it should "serialize a VideoSequence with Videos that have VideoReferences" in {
    val videoSequence = VideoSequence("Foo", "Bar", Seq(
      Video("bar1", Instant.now, videoReferences = Seq(
        VideoReference(new URI("uri:mbari:tape:T0123-04HD")),
        VideoReference(
          new URI("http://foo.bar/somevideo.mp4"),
          "video/mp4", "hevc", "pcm_s24le", 1920, 1080))),
      Video("bar2", Instant.now, Duration.ofSeconds(23))))

    val json = Constants.GSON.toJson(videoSequence)
    log.info(json)
  }

}
