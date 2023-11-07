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

import org.mbari.vampiresquid.repository.jpa.DAOSuite
import org.mbari.vampiresquid.repository.jpa.TestUtils
import scala.concurrent.ExecutionContext.Implicits.global
import org.mbari.vampiresquid.domain.VideoSequence
import org.mbari.vampiresquid.repository.jpa.BaseDAOSuite

trait  VideoSequenceControllerITSuite extends BaseDAOSuite:

  override def beforeAll(): Unit = daoFactory.beforeAll()
  override def afterAll(): Unit  = daoFactory.afterAll()

  lazy val controller = new VideoSequenceController(daoFactory)

  def assertSameValues(a: VideoSequence, b: VideoSequence): Unit =
    assertEquals(a.name, b.name)
    assertEquals(a.description, b.description)
    assertEquals(a.camera_id, b.camera_id)
    assertEquals(a.description, b.description)
  
  test("findAll"):
    val vss = TestUtils.create(2, 4, 1)
    val xs = exec(controller.findAll())
    assertEquals(xs.size, vss.size)

  test("findAllNames"):
    val vss = TestUtils.create(2, 4, 1)
    val names = vss.map(_.getName())
    val xs = exec(controller.findAllNames())
    assertEquals(xs.size, names.size)
    names.foreach(n => assert(xs.contains(n)))

  test("findAllNamesByCameraID"):
    val vss = TestUtils.create(3, 4, 1).head
    val names = Seq(vss.getName())
    val cameraID = vss.getCameraID()
    val xs = exec(controller.findAllNamesByCameraID(cameraID))
    assertEquals(xs.size, names.size)
    names.foreach(n => assert(xs.contains(n)))

  test("findAllCameraIDs"):
    val vss = TestUtils.create(5, 1, 1)
    val cameraIDs = vss.map(_.getCameraID())
    val xs = exec(controller.findAllCameraIDs())
    assertEquals(xs.size, cameraIDs.size)
    cameraIDs.foreach(n => assert(xs.contains(n)))

  test("findByUUID"):
    val vss = TestUtils.create(1, 4, 1).head
    val x = exec(controller.findByUUID(vss.getUuid()))
    assert(x.isDefined)
    assertEquals(x.get.uuid, vss.getUuid())
    assertSameValues(x.get, VideoSequence.from(vss))

  test("findByName"):
    val vss = TestUtils.create(1, 4, 1).head
    val x = exec(controller.findByName(vss.getName()))
    assert(x.isDefined)
    assertEquals(x.get.uuid, vss.getUuid())
    assertSameValues(x.get, VideoSequence.from(vss))

  test("findByCameraId"):
    val vss = TestUtils.create(3, 4, 1).head
    val cameraID = vss.getCameraID()
    val xs = exec(controller.findByCameraId(cameraID))
    assertEquals(xs.size, 1)
    assertEquals(xs.head.uuid, vss.getUuid())
    assertSameValues(xs.head, VideoSequence.from(vss))

  test("findByCameraIDAndTimestamp"):
    val vss = TestUtils.create(3, 4, 1).head
    val cameraID = vss.getCameraID()
    val timestamp = vss.getVideos().get(0).getStart()
    val xs = exec(controller.findByCameraIDAndTimestamp(cameraID, timestamp))
    assertEquals(xs.size, 1)
    assertEquals(xs.head.uuid, vss.getUuid())
    assertSameValues(xs.head, VideoSequence.from(vss))

  test("create"):
    val name = "foo"
    val cameraID = "bar"
    val description = Some("baz")
    val x = exec(controller.create(name, cameraID, description))
    assert(x.uuid != null)
    assertEquals(x.name, name)
    assertEquals(x.camera_id, cameraID)
    assertEquals(x.description, description)

  test("update"):
    val vs = TestUtils.create(1, 4, 1).head
    val name = "foo"
    val cameraID = "bar"
    val description = Some("baz")
    val x = exec(controller.update(vs.getUuid(), Some(name), Some(cameraID), description))
    assertEquals(x.uuid, vs.getUuid())
    assertEquals(x.name, name)
    assertEquals(x.camera_id, cameraID)
    assertEquals(x.description, description)

  test("delete"):
    val vs = TestUtils.create(1, 4, 1).head
    val uuid = vs.getUuid()
    val opt0 = exec(controller.findByUUID(uuid))
    assert(opt0.isDefined)
    val x = exec(controller.delete(uuid))
    assert(x)
    val opt = exec(controller.findByUUID(uuid))
    assert(opt.isEmpty)





