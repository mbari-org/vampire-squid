package org.mbari.vars.vam.messaging

import java.net.URI
import java.time.{ Duration, Instant }
import java.util.UUID

import com.google.gson.Gson
import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.dao.jpa.{ Video, VideoReference, VideoSequence }
import org.scalatest.{ FlatSpec, Inside, Matchers }

/**
 * @author Brian Schlining
 * @since 2017-03-15T09:43:00
 */
class NewVideoMessageSpec extends FlatSpec with Matchers with Inside {

  "NewVideoMessage" should "round-trip to/from JSON" in {

    val uri = new URI("uri:mbari:tape:T0123")
    val container = "video/mp4"
    val vcodec = "h264"
    val acodec = "aac"
    val width = 1920
    val height = 1080
    val videoName = getClass.getSimpleName
    val start = Instant.now
    val duration = Duration.ofMinutes(45)
    val videoSequenceName = "Foo"
    val videoSequenceUuid = UUID.randomUUID()
    val cameraId = "Ventana"

    val videoReference = VideoReference(uri, container, vcodec, acodec, width, height)
    val video = Video(videoName, start, Some(duration), Seq(videoReference))
    val videoSequence = VideoSequence(videoSequenceName, cameraId, Seq(video))
    videoSequence.uuid = videoSequenceUuid
    val msg = NewVideoMessage(videoReference)
    println(msg)
    val json = Constants.GSON.toJson(msg)
    val obj = Constants.GSON.fromJson(json, classOf[NewVideoMessage])
    obj should not be null
    inside(obj) {
      case NewVideoMessage(vsUuid, vsName, camId, videoUuid,
        vName, startTimestamp, durationMillis, vr) =>
        vsUuid should be(videoSequenceUuid)
        vsName should be(videoSequenceName)
        camId should be(cameraId)
        videoUuid should be(null)
        vName should be(videoName)
        startTimestamp should be(start)
        durationMillis should be(duration)
        vr.uri should be(uri)
        vr.container should be(container)
        vr.videoCodec should be(vcodec)
        vr.audioCodec should be(acodec)
        vr.width should be(width)
        vr.height should be(height)

    }
  }

}
