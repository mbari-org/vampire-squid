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

package org.mbari.vampiresquid.controller

import org.mbari.vampiresquid.repository.jpa.{DevelopmentTestDAOFactory, Video, VideoReference, VideoSequence}
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit

import scala.concurrent.{duration, Await, ExecutionContext}
import scala.util.Random
import scala.collection.BitSet
import org.mbari.vampiresquid.repository.jpa.entity.VideoSequenceEntity
import org.mbari.vampiresquid.repository.jpa.entity.VideoEntity
import java.{util => ju}
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity

/**
  * @author Brian Schlining
  * @since 2017-04-05T14:30:00
  */
object TestUtils {

  val DaoFactory                                  = DevelopmentTestDAOFactory
  implicit val executionContext: ExecutionContext = ExecutionContext.global
  val Timeout                                     = duration.Duration(3, TimeUnit.SECONDS)
  val Digest                                      = MessageDigest.getInstance("SHA-512")
  private[this] val random                        = Random

  def createVideoSequence(name: String, videoName: String): VideoSequenceEntity = {
    val video         = new VideoEntity(videoName, Instant.now, Duration.ofMinutes(random.nextInt(15) + 5))
    val videoSequence = new VideoSequenceEntity(name, "Tiburon", "", ju.List.of(video))
    val dao           = DaoFactory.newVideoSequenceDAO()
    val f             = dao.runTransaction(d => d.create(videoSequence))
    f.onComplete(t => dao.close())
    Await.result(f, Timeout)
    videoSequence
  }

  def randomVideoReference(): VideoReferenceEntity = {
    val v = new VideoReferenceEntity
    v.setUri(new URI(
      s"http://www.mbari.org/video/${random.nextInt(100000)}/video_${random.nextInt(100000)}.mp4"
    ))
    v.setSha512(Digest.digest(v.getUri.toString.getBytes(StandardCharsets.UTF_8)))
    v.setContainer("video/mp4")
    v.setVideoCodec("h264")
    v.setAudioCodec("aac")
    v.setWidth(random.nextInt(5000))
    v.setHeight(random.nextInt(3000))
    v.setFrameRate(random.nextInt(50) + 10)
    v.setDescription(s"This is dive ${random.nextInt(10000)}")
    v.setSize(random.nextInt(9999999))
    v
  }

  def randomSha512(): Array[Byte] = Array.fill[Byte](64)((Random.nextInt(256) - 128).toByte)

  def create(numVideoSeqs: Int, numVideos: Int, numVideoRef: Int): Seq[VideoSequenceEntity] = {
    val longTimeout = duration.Duration(numVideoSeqs * 2, TimeUnit.SECONDS)
    for (i <- 0 until numVideoSeqs) yield {
      val videoSequence =
        new VideoSequenceEntity(s"A${random.nextInt()} B${random.nextInt()}", s"AUV ${random.nextInt()}")

      for (_ <- 0 until numVideos) {
        val video = new VideoEntity(
          videoSequence.getName + s"_C${random.nextInt()}",
          Instant.ofEpochSecond(math.abs(random.nextInt())),
          Duration.ofMinutes(random.nextInt(15) + 1),
        )
        video.setDescription(s"Some description ${random.nextInt()}")
        videoSequence.addVideo(video)

        for (i <- 0 until numVideoRef) {
          val videoReference = randomVideoReference()
          video.addVideoReference(videoReference)
        }
      }

      val dao = DaoFactory.newVideoSequenceDAO()
      val f   = dao.runTransaction(d => d.create(videoSequence))
      f.onComplete(_ => dao.close())
      Await.result(f, longTimeout)
      videoSequence

    }
  }

}
