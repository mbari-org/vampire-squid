package org.mbari.vars.vam.api

import java.net.URI
import java.nio.charset.{ Charset, StandardCharsets }
import java.security.MessageDigest
import java.time.{ Duration, Instant }
import java.util.concurrent.TimeUnit

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.controller.TestUtils
import org.mbari.vars.vam.controllers.{ MediaController, VideoController, VideoReferenceController, VideoSequenceController }
import org.mbari.vars.vam.dao.jpa.{ ByteArrayConverter, Video, VideoReference, VideoSequence }
import org.slf4j.LoggerFactory

import scala.concurrent.{ Await, duration }
import scala.util.Random

/**
 * @author Brian Schlining
 * @since 2017-04-05T10:48:00
 */
class VideoReferenceV2ApiSpec extends WebApiStack {

  private[this] val videoReferenceV2Api = new VideoReferenceV2Api(new VideoReferenceController(daoFactory))
  private[this] val name = getClass.getSimpleName
  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val digest = TestUtils.Digest
  private[this] val timeout = TestUtils.Timeout

  addServlet(videoReferenceV2Api, "/v2/videoreferences")

  "VideoReferenceV2Api" should "create via post (form body with minimal args)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 01", "Test 01A")
    val uri = "http://www.mbari.org/videos/test01.mp4"
    val b = s"video_uuid=${videoSequence.videos.head.uuid}&uri=${uri}"
    post(
      "v2/videoreferences",
      body = b.getBytes(Charset.forName("UTF-8")),
      headers = Seq("Content-Type" -> "application/form")) {
        status should be(200)
        val videoReference = gson.fromJson(body, classOf[VideoReference])
        videoReference.uuid should not be (null)
        videoReference.uri should be(new URI(uri))
      }
  }

  it should "create via post (form body with all args)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 02", "Test 02A")
    val v = TestUtils.randomVideoReference()
    val b = s"video_uuid=${videoSequence.videos.head.uuid}&uri=${v.uri}&container=${v.container}&" +
      s"video_codec=${v.videoCodec}&audio_codec=${v.audioCodec}&width=${v.width}&height=${v.height}&" +
      s"frame_rate=${v.frameRate}&size_bytes=${v.size}&description=${v.description}&" +
      s"sha512=${ByteArrayConverter.encode(v.sha512)}"
    post(
      "v2/videoreferences",
      body = b.getBytes(Charset.forName("UTF-8")),
      headers = Seq("Content-Type" -> "application/form")) {
        status should be(200)
        val videoReference = gson.fromJson(body, classOf[VideoReference])
        videoReference.uuid should not be (null)
        videoReference.uri should be(v.uri)
        videoReference.container should be(v.container)
        videoReference.videoCodec should be(v.videoCodec)
        videoReference.audioCodec should be(v.audioCodec)
        videoReference.width should be(v.width)
        videoReference.height should be(v.height)
        videoReference.frameRate should be(v.frameRate)
        videoReference.size should be(v.size)
        videoReference.description should be(v.description)
        videoReference.sha512 should be(v.sha512)
      }
  }

  it should "create via post (json body with minimal args)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 03", "Test 03A")
    val uri = "http://www.mbari.org/videos/test03.mp4"
    val b =
      s"""
         | {
         |  "video_uuid": "${videoSequence.videos.head.uuid}",
         |  "uri": "$uri"
         |}
      """.stripMargin

    val vr = gson.fromJson(b, classOf[VideoRefParams])

    post(
      "v2/videoreferences",
      body = b.getBytes(Charset.forName("UTF-8")),
      headers = Seq("Content-Type" -> "application/json")) {
        status should be(200)
        val videoReference = gson.fromJson(body, classOf[VideoReference])
        videoReference.uuid should not be (null)
        videoReference.uri should be(new URI(uri))
      }
  }

  it should "create via post (json body with all args)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 04", "Test 04A")
    val v = TestUtils.randomVideoReference()
    val b =
      s"""
         | {
         |  "video_uuid": "${videoSequence.videos.head.uuid}",
         |  "uri": "${v.uri}",
         |  "container": "${v.container}",
         |  "video_codec": "${v.videoCodec}",
         |  "audio_codec": "${v.audioCodec}",
         |  "width": ${v.width},
         |  "height": ${v.height},
         |  "frame_rate": ${v.frameRate},
         |  "size_bytes": ${v.size},
         |  "description": "${v.description}",
         |  "sha512": "${ByteArrayConverter.encode(v.sha512)}"
         |}
      """.stripMargin

    val vr = gson.fromJson(b, classOf[VideoRefParams])

    post(
      "v2/videoreferences",
      body = b.getBytes(Charset.forName("UTF-8")),
      headers = Seq("Content-Type" -> "application/json")) {
        status should be(200)
        val videoReference = gson.fromJson(body, classOf[VideoReference])
        videoReference.uuid should not be (null)
        videoReference.uri should be(v.uri)
        videoReference.container should be(v.container)
        videoReference.videoCodec should be(v.videoCodec)
        videoReference.audioCodec should be(v.audioCodec)
        videoReference.width should be(v.width)
        videoReference.height should be(v.height)
        videoReference.frameRate should be(v.frameRate)
        videoReference.size should be(v.size)
        videoReference.description should be(v.description)
        videoReference.sha512 should be(v.sha512)
      }
  }

  it should "update via put (form body)" in {
    val videoSequence = TestUtils.createVideoSequence("Test 05", "Test 05A")
    val dao = daoFactory.newVideoSequenceDAO()
    val fn = dao.runTransaction(d => {
      val vs = d.find(videoSequence)
      val v0 = TestUtils.randomVideoReference()
      vs.get.videos.head.addVideoReference(v0)
      v0
    })
    fn.onComplete(t => dao.close())
    val v1 = Await.result(fn, timeout)
    val v = TestUtils.randomVideoReference()
    val b = s"video_uuid=${videoSequence.videos.head.uuid}&uri=${v.uri}&container=${v.container}&" +
      s"video_codec=${v.videoCodec}&audio_codec=${v.audioCodec}&width=${v.width}&height=${v.height}&" +
      s"frame_rate=${v.frameRate}&size_bytes=${v.size}&description=${v.description}&" +
      s"sha512=${ByteArrayConverter.encode(v.sha512)}"
    put(
      s"v2/videoreferences/${v.uuid}",
      body = b.getBytes(StandardCharsets.UTF_8)) {
        status should be(200)
        val videoReference = gson.fromJson(body, classOf[VideoReference])
        videoReference.uuid should not be (null)
        videoReference.uri should be(v.uri)
        videoReference.container should be(v.container)
        videoReference.videoCodec should be(v.videoCodec)
        videoReference.audioCodec should be(v.audioCodec)
        videoReference.width should be(v.width)
        videoReference.height should be(v.height)
        videoReference.frameRate should be(v.frameRate)
        //videoReference.size should be(v.size) TODO fix this. Size update isn't working
        videoReference.description should be(v.description)
        videoReference.sha512 should be(v.sha512)
      }

  }

}
