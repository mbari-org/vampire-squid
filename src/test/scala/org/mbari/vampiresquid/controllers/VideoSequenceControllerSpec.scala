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

import org.mbari.vampiresquid.controllers.VideoSequenceController
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.jdk.CollectionConverters._
import org.mbari.vampiresquid.repository.jpa.{TestDAOFactory, TestUtils}

/**
  * @author Brian Schlining
  * @since 2017-04-05T14:28:00
  */
class VideoSequenceControllerSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll:

  private[this] val n              = 2
  private[this] val videoSequences = TestUtils.create(n, 10, 10)
  private[this] val controller     = new VideoSequenceController(TestUtils.DaoFactory)
  import TestUtils.executionContext

  "VideoSequenceController" should "findAll" in:
    val fn = controller.findAll()
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)

  it should "findAllNames" in:
    val fn = controller.findAllNames()
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)

  it should "findByUUID" in:
    val fn = controller.findAll()
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
    val uuid = rs.head.uuid
    val fn0  = controller.findByUUID(uuid)
    val rs0  = Await.result(fn0, TestUtils.Timeout)
    rs0 should not be (empty)
    rs0.get.uuid should be(uuid)

  it should "findAllCameraIDs" in:
    val fn = controller.findAllCameraIDs()
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)

  it should "findByName" in:
    val fn = controller.findAll()
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
    val name = rs.head.name
    val fn0  = controller.findByName(name)
    val rs0  = Await.result(fn0, TestUtils.Timeout)
    rs0 should not be (empty)
    rs0.get.name should be(name)

  it should "findByCameraIDAndTimestamp" in:
    val fn = controller.findAll()
    val rs = Await.result(fn, TestUtils.Timeout)
    rs.size should be(n)
    val cameraId  = rs.head.cameraId
    val timestamp = rs.head.videos.head.start
    val fn0       = controller.findByCameraIDAndTimestamp(cameraId, timestamp)
    val rs0       = Await.result(fn0, TestUtils.Timeout)
    rs0 should not be (empty)

  override protected def afterAll(): Unit = TestUtils.DaoFactory.cleanup()
