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

package org.mbari.vampiresquid.repository.jpa

import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.repository.VideoDAO

import java.time.{Duration, Instant}
import java.util.UUID
import jakarta.persistence.EntityManager

import scala.jdk.CollectionConverters.*
import org.mbari.vampiresquid.repository.jpa.entity.{NameAndDate, VideoEntity}

import scala.util.chaining.*
import java.sql.Timestamp


/** @author
  *   Brian Schlining
  * @since 2016-05-11T14:35:00
  */
class VideoDAOImpl(entityManager: EntityManager) extends BaseDAO[VideoEntity](entityManager) with VideoDAO[VideoEntity]:

  override def findByName(name: String): Option[VideoEntity] =
    findByNamedQuery("Video.findByName", Map("name" -> name)).headOption

  override def findByVideoSequenceUUID(uuid: UUID): Iterable[VideoEntity] =
    findByNamedQuery("Video.findByVideoSequenceUUID", Map("uuid" -> uuid))

  override def findByVideoReferenceUUID(uuid: UUID): Option[VideoEntity] =
    findByNamedQuery("Video.findByVideoReferenceUUID", Map("uuid" -> uuid)).headOption

  override def findByTimestamp(
      timestamp: Instant,
      window: Duration = Constants.DEFAULT_DURATION_WINDOW
  ): Iterable[VideoEntity] =
    val halfRange    = window.dividedBy(2)
    val startDate    = timestamp.minus(halfRange)
    val endDate      = timestamp.plus(halfRange)
    val videos       = findByNamedQuery(
      "Video.findBetweenDates",
      Map("startDate" -> startDate, "endDate" -> endDate)
    )
    val hasTimestamp = containsTimestamp(_: VideoEntity, timestamp)
    videos.filter(hasTimestamp)

  override def findBetweenTimestamps(t0: Instant, t1: Instant): Iterable[VideoEntity] =
    findByNamedQuery("Video.findBetweenDates", Map("startDate" -> t0, "endDate" -> t1))

  override def findAll(): Iterable[VideoEntity] = findByNamedQuery("Video.findAll")

  override def deleteByUUID(primaryKey: UUID): Unit =
    val video = findByUUID(primaryKey)
    video.foreach(v => delete(v))

  private def containsTimestamp(video: VideoEntity, timestamp: Instant): Boolean =
    val startDate = video.getStart()
    val endDate   = video.getStart.plus(video.getDuration())

    startDate.equals(timestamp) ||
    endDate.equals(timestamp) ||
    (startDate.isBefore(timestamp) && endDate.isAfter(timestamp))

  override def findAllNames(): Iterable[String] =
    entityManager
      .createNamedQuery("Video.findAllNames")
      .getResultList
      .asScala
      .map(_.toString)

  override def findAllNamesAndTimestamps(): Iterable[(String, Instant)] =
    entityManager
      .createNamedQuery("Video.findAllNamesAndStartDates")
      .getResultList
      .asScala
      .map(r => r.asInstanceOf[NameAndDate])
      .map(r => (r.name, r.date))


  def findNamesByVideoSequenceName(videoSequenceName: String): Iterable[String] =
    val query = entityManager.createNamedQuery("Video.findNamesByVideoSequenceName")
    query.setParameter(1, videoSequenceName)
    query
      .getResultList
      .asScala
      .map(_.toString)
