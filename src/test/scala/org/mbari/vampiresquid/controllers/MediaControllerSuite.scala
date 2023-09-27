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

package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.repository.VideoReferenceDAO
import org.mbari.vampiresquid.repository.jpa.DAOSuite
import org.mbari.vampiresquid.repository.jpa.TestUtils
import org.mbari.vampiresquid.domain.Media
import scala.concurrent.ExecutionContext.Implicits.global
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity
import java.time.Duration
import java.util.UUID
import org.mbari.vampiresquid.etc.jdk.Uris
import org.junit.Assert
import java.util.Arrays


class MediaControllerSuite extends DAOSuite:

  private val controller = MediaController(daoFactory)

  def buildMedia(): Media =
    val vs = TestUtils.build(1, 1, 1)
    val vr = vs.head.getVideoReferences().get(0)
    Media.from(vr)

  def createMedia(): Media =
    exec(controller.createMedia(buildMedia()))

  def assertSameValues(m0: Media, m1: Media): Unit =
    Assert.assertArrayEquals(m0.sha512.orNull, m1.sha512.orNull)
    assertEquals(m0.video_sequence_name, m1.video_sequence_name)
    assertEquals(m0.video_name, m1.video_name)
    assertEquals(m0.camera_id, m1.camera_id)
    assertEquals(m0.start_timestamp, m1.start_timestamp)
    assertEquals(m0.duration, m1.duration)
    assertEquals(m0.uri, m1.uri)
    assertEquals(m0.container, m1.container)
    assertEquals(m0.video_codec, m1.video_codec)
    assertEquals(m0.audio_codec, m1.audio_codec)
    assertEquals(m0.width, m1.width)
    assertEquals(m0.height, m1.height)
    assertEquals(m0.frame_rate, m1.frame_rate)
    assertEquals(m0.size_bytes, m1.size_bytes)
    assertEquals(m0.description, m1.description)
    assertEquals(m0.video_description, m1.video_description)
    assertEquals(m0.video_sequence_description, m1.video_sequence_description)

  test("createMedia"):
    val m0 = buildMedia()
    val m1 = exec(controller.createMedia(m0))
    assertSameValues(m1, m0)

  test("create"):
    val m0 = buildMedia()
    val m1 = exec(
      controller.create(
        m0.video_sequence_name,
        m0.camera_id,
        m0.video_name,
        m0.uri,
        m0.start_timestamp,
        m0.duration,
        m0.container,
        m0.video_codec,
        m0.audio_codec,
        m0.width,
        m0.height,
        m0.frame_rate,
        m0.size_bytes,
        m0.description,
        m0.sha512,
        m0.video_sequence_description,
        m0.video_description
      )
    )
    assertSameValues(m1, m0)

  test("updateMedia"):
    val m0 = createMedia()
    val m1 = m0.copy(video_sequence_name = "foobarbaz", video_name = "foobarbazbim", video_codec = Some("video/magick"))
    val opt = exec(controller.updateMedia(m1))
    assert(opt.isDefined)
    val m2 = opt.get
    assertSameValues(m2, m1)

  test("findAndUpdate"):
    val m0 = createMedia()
    val m1 = m0.copy(video_sequence_name = "foobarbaz", video_name = "foobarbazbim", video_codec = Some("video/magick"))
    def find(dao: VideoReferenceDAO[VideoReferenceEntity]): Option[VideoReferenceEntity] = 
      dao.findByUUID(m0.video_reference_uuid)
    val opt = exec(controller.findAndUpdate(findFn = find, 
        videoSequenceName = m1.video_sequence_name,
        cameraId = m1.camera_id,
        videoName = m1.video_name,
        videoCodec = m1.video_codec))
    assert(opt.isDefined)
    val m2 = opt.get
    assertSameValues(m2, m1)

  test("update"):
    val m0 = createMedia()
    val m1 = m0.copy(video_sequence_name = "foobarbaz", video_name = "foobarbazbim", video_codec = Some("video/magick"))
    val opt = exec(controller.update(m1.sha512.get,
      m1.videoSequenceName,
      m1.camera_id,
      m1.videoName,
      videoCodec = m1.videoCodec))
    assert(opt.isDefined)
    assertSameValues(opt.get, m1)

  test("moveVideoReference"):
    val m0 = createMedia()
    val m1 = createMedia()

    // move to existing
    val opt = exec(controller.moveVideoReference(m0.video_reference_uuid, m1.video_name, m1.start_timestamp, m1.duration.get))
    assert(opt.isDefined)
    val m2 = opt.get
    assertEquals(m2.video_sequence_name, m1.video_sequence_name)
    assertEquals(m2.video_name, m1.video_name)
    assertEquals(m2.start_timestamp, m1.start_timestamp)
    assertEquals(m2.duration.get, m1.duration.get)

    // move to new
    val newName = "one more ref"
    val newDuration = Duration.ofSeconds(100)
    val opt1 = exec(controller.moveVideoReference(m0.video_reference_uuid, newName, m1.start_timestamp, Duration.ofSeconds(100)))
    assert(opt1.isDefined)
    val m3 = opt1.get
    assertEquals(m2.video_sequence_name, m1.video_sequence_name)
    assertEquals(m3.video_name, newName)
    assertEquals(m3.start_timestamp, m2.start_timestamp)
    assertEquals(m3.duration.get, newDuration)

    // move bogus uuid
    val newUuid = UUID.randomUUID()
    val opt2 = exec(controller.moveVideoReference(newUuid, newName, m1.start_timestamp, Duration.ofSeconds(100)))
    assert(opt2.isEmpty)
    

  test("findByVideoReferenceUuid"):
    val m0 = createMedia()
    val opt = exec(controller.findByVideoReferenceUuid(m0.video_reference_uuid))
    assert(opt.isDefined)
    assertSameValues(opt.get, m0)

  test("findBySha512"):
    val m0 = createMedia()
    val opt = exec(controller.findBySha512(m0.sha512.get))
    assert(opt.isDefined)
    assertSameValues(opt.get, m0)

  test("findByVideoSequenceName"):
    val m0 = createMedia()
    val xs = exec(controller.findByVideoSequenceName(m0.video_sequence_name))
    assertEquals(xs.size, 1)
    assertSameValues(xs.head, m0)

  test("findByVideoSequenceNameAndTimestamp"):
    val m0 = createMedia()
    val xs = exec(controller.findByVideoSequenceNameAndTimestamp(m0.video_sequence_name, m0.start_timestamp.plus(m0.duration.get.dividedBy(2))))
    assertEquals(xs.size, 1)
    assertSameValues(xs.head, m0)

  test("findByCameraIdAndTimestamp"):
    val m0 = createMedia()
    val xs = exec(controller.findByCameraIdAndTimestamp(m0.camera_id, m0.start_timestamp.plus(m0.duration.get.dividedBy(2))))
    assertEquals(xs.size, 1)
    assertSameValues(xs.head, m0)

  test("findByCameraIdAndTimestamps"):
    val m0 = createMedia()
    val xs = exec(controller.findByCameraIdAndTimestamps(m0.camera_id, m0.start_timestamp, m0.start_timestamp.plus(m0.duration.get)))
    assertEquals(xs.size, 1)
    assertSameValues(xs.head, m0)

  test("findConcurrent"):
    val m0 = createMedia()
    val xs = exec(controller.findConcurrent(m0.video_reference_uuid))
    assertEquals(xs.size, 1)
    assertSameValues(xs.head, m0)
    // TODO create more than one concurrent video

  test("findByVideoName"):
    val m0 = createMedia()
    val xs = exec(controller.findByVideoName(m0.video_name))
    assertEquals(xs.size, 1)
    assertSameValues(xs.head, m0)

  test("findByURI"):
    val m0 = createMedia()
    val xs = exec(controller.findByURI(m0.uri))
    assertEquals(xs.size, 1)
    assertSameValues(xs.head, m0)

  test("findByFileName"):
    val m0 = createMedia()
    val filename = Uris.filename(m0.uri)
    val xs = exec(controller.findByFileName(filename))
    assertEquals(xs.size, 1)
    assertSameValues(xs.head, m0)