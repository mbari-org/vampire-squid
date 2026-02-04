/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
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

import jakarta.persistence.{EntityManager, Transient}
import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.repository.VideoSequenceDAO
import org.mbari.vampiresquid.repository.jpa.entity.VideoSequenceEntity
import org.slf4j.LoggerFactory

import java.time.{Duration, Instant}
import java.util.UUID
import scala.jdk.CollectionConverters.*
import scala.util.Try

/**
 * @author
 *   Brian Schlining
 * @since 2016-05-06T14:20:00
 */
class VideoSequenceDAOImpl(entityManager: EntityManager)
    extends BaseDAO[VideoSequenceEntity](entityManager)
    with VideoSequenceDAO[VideoSequenceEntity]:

    @Transient
    private val log = LoggerFactory.getLogger(getClass)

    override def findEmptyNames(): Iterable[String] = 
        val query = entityManager.createNamedQuery("VideoSequence.listEmpty", classOf[VideoSequenceEntity])
        query
            .getResultList
            .asScala
            .map(_.getName)
            .toSet

    override def findByCameraID(cameraID: String): Iterable[VideoSequenceEntity] =
        findByNamedQuery("VideoSequence.findByCameraID", Map("cameraID" -> cameraID))

    override def findByName(name: String): Option[VideoSequenceEntity] =
        findByNamedQuery("VideoSequence.findByName", Map("name" -> name)).headOption

    override def findByVideoUUID(uuid: UUID): Option[VideoSequenceEntity] =
        findByNamedQuery("VideoSequence.findByVideoUUID", Map("uuid" -> uuid)).headOption

    override def findByVideoReferenceUUID(uuid: UUID): Option[VideoSequenceEntity] =
        findByNamedQuery("VideoSequence.findByVideoReferenceUUID", Map("uuid" -> uuid)).headOption

    /**
     * @param timestamp
     *   The moment of interest
     * @param window
     *   A search window that so that the actual search is timestamp +/- (range / 2)
     * @return
     */
    override def findByTimestamp(
        timestamp: Instant,
        window: Duration = Constants.DEFAULT_DURATION_WINDOW
    ): Iterable[VideoSequenceEntity] =
        val halfRange = window.dividedBy(2)
        val startDate = timestamp.minus(halfRange)
        val endDate   = timestamp.plus(halfRange)

        val videoSequences = findByNamedQuery(
            "VideoSequence.findBetweenDates",
            Map("startDate" -> startDate, "endDate" -> endDate)
        )

        if log.isDebugEnabled then
            val info = videoSequences
                .flatMap(_.getVideos().asScala)
                .map(v => s"\t${v.getName()} at ${v.getStart()} for ${v.getDuration()}")
                .mkString("\n")
            val s    =
                s"""
           | Found ${videoSequences.size} VideoSequences between $startDate and $endDate
           | $info
         """.stripMargin
            log.debug(s)

        val hasTimestamp =
            containsTimestamp(_: VideoSequenceEntity, timestamp) // Partially apply the function to timestamp

        videoSequences.filter(hasTimestamp).toSet

    override def findByNameAndTimestamp(
        name: String,
        timestamp: Instant,
        window: Duration = Constants.DEFAULT_DURATION_WINDOW
    ): Iterable[VideoSequenceEntity] =
        val halfRange      = window.dividedBy(2)
        val startDate      = timestamp.minus(halfRange)
        val endDate        = timestamp.plus(halfRange)
        val videoSequences = findByNamedQuery(
            "VideoSequence.findByNameAndBetweenDates",
            Map("startDate" -> startDate, "endDate" -> endDate, "name" -> name)
        )

        val hasTimestamp =
            containsTimestamp(_: VideoSequenceEntity, timestamp) // Partially apply the function to timestamp

        videoSequences.filter(hasTimestamp).toSet

    override def findByCameraIDAndTimestamp(
        cameraID: String,
        timestamp: Instant,
        window: Duration = Constants.DEFAULT_DURATION_WINDOW
    ): Iterable[VideoSequenceEntity] =
        val halfRange = window.dividedBy(2)
        val startDate = timestamp.minus(halfRange)
        val endDate   = timestamp.plus(halfRange)

        val videoSequences = findByNamedQuery(
            "VideoSequence.findByCameraIDAndBetweenDates",
            Map("startDate" -> startDate, "endDate" -> endDate, "cameraID" -> cameraID)
        )

        val hasTimestamp =
            containsTimestamp(_: VideoSequenceEntity, timestamp) // Partially apply the function to timestamp

        videoSequences.filter(hasTimestamp).toSet

    override def deleteByUUID(primaryKey: UUID): Unit =
        findByUUID(primaryKey).foreach(delete)

    private def containsTimestamp(vs: VideoSequenceEntity, timestamp: Instant): Boolean =
        vs.getVideos()
            .asScala
            .map(v => (v.getStart, Try(v.getStart.plus(v.getDuration())).getOrElse(v.getStart())))
            .exists { case (a, b) =>
                a.equals(timestamp) ||
                b.equals(timestamp) ||
                (a.isBefore(timestamp) && b.isAfter(timestamp))
            }

    override def findAll(offset: Int, limit: Int): Iterable[VideoSequenceEntity] =
        findByNamedQuery("VideoSequence.findAll", offset = Some(offset), limit = Some(limit))

    override def findAllNames(): Iterable[String] =
        entityManager
            .createNamedQuery("VideoSequence.findAllNames")
            .getResultList
            .asScala
            .map(_.toString)

    override def findAllCameraIDs(): Iterable[String] =
        entityManager
            .createNamedQuery("VideoSequence.findAllCameraIDs")
            .getResultList
            .asScala
            .map(_.toString)

    override def findAllNamesByCameraID(cameraID: String): Iterable[String] =
        val query = entityManager.createNamedQuery("VideoSequence.findNamesByCameraID")
        query.setParameter(1, cameraID)
        query
            .getResultList
            .asScala
            .map(_.toString)
