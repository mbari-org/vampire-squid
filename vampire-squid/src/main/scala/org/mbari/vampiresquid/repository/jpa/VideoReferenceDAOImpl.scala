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

import org.mbari.vampiresquid.repository.VideoReferenceDAO

import java.net.URI
import java.util.UUID
import jakarta.persistence.EntityManager
import scala.jdk.CollectionConverters._

import scala.util.control.NonFatal
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity

/** @author
  *   Brian Schlining
  * @since 2016-05-18T13:11:00
  */
class VideoReferenceDAOImpl(entityManager: EntityManager)
    extends BaseDAO[VideoReferenceEntity](entityManager)
    with VideoReferenceDAO[VideoReferenceEntity]:

  override def findByVideoUUID(uuid: UUID): Iterable[VideoReferenceEntity] =
    findByNamedQuery("VideoReference.findByVideoUUID", Map("uuid" -> uuid))

  override def findByURI(uri: URI): Option[VideoReferenceEntity] =
    findByNamedQuery("VideoReference.findByURI", Map("uri" -> uri)).headOption

  override def findByFileName(filename: String): Iterable[VideoReferenceEntity] =
    findByNamedQuery("VideoReference.findByFileName", Map("filename" -> s"%$filename"))

  override def findAll(): Iterable[VideoReferenceEntity] =
    findByNamedQuery("VideoReference.findAll")

  override def findAllURIs(): Iterable[URI] =
    val query = entityManager.createNamedQuery("VideoReference.findAllURIs")
    query
      .getResultList
      .asScala
      .map(_.toString)
      .map(URI.create)

  def findConcurrent(uuid: UUID): Iterable[VideoReferenceEntity] =
    findByUUID(uuid) match
      case None                 => Nil
      case Some(videoReference) =>
        val video     = videoReference.getVideo
        val startDate = video.getStart
        val endDate   = startDate.plus(video.getDuration)
        val siblings  = video.getVideoSequence.getVideoReferences.asScala

        def filterSiblings(vr: VideoReferenceEntity): Boolean =
          try
            val s = vr.getVideo.getStart
            if (s == null) false
            else
              val e = s.plus(vr.getVideo.getDuration)
              s.equals(startDate) ||
              e.equals(endDate) ||
              (s.isAfter(startDate) && s.isBefore(endDate)) ||
              (e.isAfter(startDate) && e.isBefore(endDate)) ||
              (s.isBefore(startDate) && e.isAfter(endDate))
          catch
            case NonFatal(e) =>
              false // Can occur if duration is null

        siblings.filter(filterSiblings)

  override def deleteByUUID(primaryKey: UUID): Unit =
    val videoReference = findByUUID(primaryKey)
    videoReference.foreach(vr => delete(vr))

  override def findBySha512(sha: Array[Byte]): Option[VideoReferenceEntity] =
    // val shaEncoded = Base64.getEncoder.encodeToString(sha)
    findByNamedQuery("VideoReference.findBySha512", Map("sha512" -> sha)).headOption
