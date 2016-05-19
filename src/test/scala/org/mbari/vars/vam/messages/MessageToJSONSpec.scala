package org.mbari.vars.vam.messages

import java.net.URI
import java.time.{Duration, Instant}
import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-19T13:47:00
  */
class MessageToJSONSpec extends FlatSpec with Matchers {

  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val gson = Constants.GSON

  "GSON" should "convert a CreateVideo message to JSON" in {

    val msg = CreateVideo(UUID.randomUUID(), "foo", Instant.now(), Duration.ofSeconds(10))

    val json = gson.toJson(msg)
    log.debug(json)
    val createVideo = gson.fromJson(json, classOf[CreateVideo])
    createVideo should not be (null)
    createVideo.videoSequence should be (msg.videoSequence)
  }

  it should "convert a CreateVideo message with null duration to JSON" in {

    val msg = CreateVideo(UUID.randomUUID(), "foo", Instant.now(), null)

    val json = gson.toJson(msg)
    log.debug(json)
    val createVideo = gson.fromJson(json, classOf[CreateVideo])
    createVideo should not be (null)
    createVideo.videoSequence should be (msg.videoSequence)
    createVideo.duration should be (null)
  }

  it should "convert a CreateVideoReference message to JSON" in {
    val msg = CreateVideoReference(UUID.randomUUID(), new URI("urn:foo"), "video/mp4", "hevc",
      "aac", 1920, 1080, 30, 99776655)

    val json = gson.toJson(msg)
    log.debug(json)
    val createVideoReference = gson.fromJson(json, classOf[CreateVideoReference])
    createVideoReference should not be (null)
    createVideoReference.video should be (msg.video)
    createVideoReference.uri should be (msg.uri)
  }

  it should "convert a CreateVideoReference message with null fields to JSON" in {
    val msg = CreateVideoReference(UUID.randomUUID(), new URI("urn:foo"), null, "hevc", "aac",
      1920)

    val json = gson.toJson(msg)
    log.debug(json)
    val createVideoReference = gson.fromJson(json, classOf[CreateVideoReference])
    createVideoReference should not be (null)
    createVideoReference.video should be (msg.video)
    createVideoReference.uri should be (msg.uri)
  }

}
