package org.mbari.vars.vam.api

import java.nio.charset.StandardCharsets
import java.time.{ Duration, Instant }
import java.util.UUID

import org.mbari.vars.vam.controller.TestUtils
import org.mbari.vars.vam.controllers.VideoController
import org.mbari.vars.vam.dao.jpa.Video
import org.slf4j.LoggerFactory

import scala.util.Random

/**
 * @author Brian Schlining
 * @since 2017-04-05T16:17:00
 */
class VideoV2ApiSpec extends WebApiStack {

  private[this] val videoV2Api = new VideoV2Api(new VideoController(daoFactory))
  private[this] val name = getClass.getSimpleName
  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val digest = TestUtils.Digest
  private[this] val timeout = TestUtils.Timeout

  addServlet(videoV2Api, "/v2/videos")

  "VideoV2Api" should "create via post (form body w/ minimal args)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 001", "Test 001A")
    val video = createVideoParams("Test 001B", videoSequence.uuid, Instant.now)
    val b = s"name=${video.name}&video_sequence_uuid=${videoSequence.uuid}&" +
      s"start_timestamp=${Instant.now()}"
    post(
      "v2/videos",
      body = b.getBytes(StandardCharsets.UTF_8),
      headers = Seq("Content-Type" -> "application/form")) {

        status should be(200)
        val vid = gson.fromJson(body, classOf[Video])
        vid.uuid should not be (null)
        vid.name should be(video.name)
        vid.start should be(video.start)
      }
  }

  it should "create via post (form body w/ all args)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 002", "Test 002A")
    val video = createVideoParams(
      "Test 002B",
      videoSequence.uuid,
      Instant.now,
      Duration.ofMinutes(Random.nextInt(15) + 5),
      "Foo")
    val b = s"name=${video.name}&video_sequence_uuid=${videoSequence.uuid}&" +
      s"start_timestamp=${Instant.now()}&duration_millis=${video.duration.toMillis}&" +
      s"description=${video.description}"
    post(
      "v2/videos",
      body = b.getBytes(StandardCharsets.UTF_8),
      headers = Seq("Content-Type" -> "application/form")) {

        status should be(200)
        val vid = gson.fromJson(body, classOf[Video])
        vid.uuid should not be (null)
        vid.name should be(video.name)
        vid.start should be(video.start)
        vid.duration should be(video.duration)
        vid.description should be(video.description)
      }
  }

  it should "create via post (json body w/ minimal args)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 003", "Test 003A")
    val video = createVideoParams("Test 003B", videoSequence.uuid, Instant.now)
    val b = gson.toJson(video)
    post(
      "v2/videos",
      body = b.getBytes(StandardCharsets.UTF_8),
      headers = Seq("Content-Type" -> "application/json")) {

        status should be(200)
        val vid = gson.fromJson(body, classOf[Video])
        vid.uuid should not be (null)
        vid.name should be(video.name)
        vid.start should be(video.start)
      }
  }

  it should "create via post (json body w/ all args)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 004", "Test 004A")
    val video = createVideoParams(
      "Test 004B",
      videoSequence.uuid,
      Instant.now,
      Duration.ofMinutes(Random.nextInt(15) + 5),
      "Foo")
    val b = gson.toJson(video)
    post(
      "v2/videos",
      body = b.getBytes(StandardCharsets.UTF_8),
      headers = Seq("Content-Type" -> "application/json")) {

        status should be(200)
        val vid = gson.fromJson(body, classOf[Video])
        vid.uuid should not be (null)
        vid.name should be(video.name)
        vid.start should be(video.start)
        vid.duration should be(video.duration)
        vid.description should be(video.description)
      }
  }

  def createVideoParams(name: String, videoSequenceUuid: UUID, start: Instant): VideoParams = {
    val v = new VideoParams
    v.name = name
    v.videoSequenceUuud = videoSequenceUuid
    v.start = start
    v
  }

  def createVideoParams(name: String, videoSequenceUuid: UUID, start: Instant,
    duration: Duration, description: String): VideoParams = {
    val v = new VideoParams
    v.name = name
    v.videoSequenceUuud = videoSequenceUuid
    v.start = start
    v.duration = duration
    v.description = description
    v
  }

}
