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

package org.mbari.vars.vam.controller

import java.time.{ Duration, Instant }
import java.util.TimeZone
import java.util.concurrent.TimeUnit

import org.mbari.vars.vam.controllers.{ VideoController, VideoSequenceController }
import org.mbari.vars.vam.dao.jpa._
import org.scalatest.{ BeforeAndAfterEach, FlatSpec, Matchers }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration => SDuration }
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-08-12T10:56:00
 */
class VideoControllerSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

  val daoFactory = DevelopmentTestDAOFactory

  private[this] val controller = new VideoController(daoFactory)
  private[this] val vsController = new VideoSequenceController(daoFactory)

  private[this] val duration = Duration.ofMinutes(15)
  private[this] val timeout = SDuration(2, TimeUnit.SECONDS)
  private[this] val now = Instant.now()

  "VideoController" should "create" in {
    val fn0 = vsController.create("A VideoSequence", "Thundar")
    val vs = Await.result(fn0, timeout)
    vs should not be (null)

    val fn1 = controller.create(vs.uuid, "Proxy", now, Some(duration))
    val video = Await.result(fn1, timeout)
    video should not be null

  }

  it should "find by timestamp" in {
    val videos = Await.result(controller.findByTimestamp(now.plusMillis(1000)), timeout)
    videos.size should be(1)
  }

  daoFactory.cleanup()

}
