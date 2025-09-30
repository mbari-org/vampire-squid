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

import jakarta.persistence.EntityManager
import org.mbari.vampiresquid.domain.Media
import org.mbari.vampiresquid.repository.MediaDAO
import org.mbari.vampiresquid.repository.jpa.entity.Media as MediaDTO

import scala.jdk.CollectionConverters.*

class MediaDAOImpl(videoSequenceDao: VideoSequenceDAOImpl) extends MediaDAO:

    val entityManager: EntityManager = videoSequenceDao.entityManager

    def findByNames(names: Iterable[String], offset: Option[Int] = None, limit: Option[Int] = None): Seq[Media] =
        findByNamedQuery[MediaDTO]("VideoSequence.findMediaByNames", Map("names" -> names.asJava), offset, limit)
            .map(Media.from(_))

    def findByNamedQuery[B](
        name: String,
        namedParameters: Map[String, Any] = Map.empty,
        offset: Option[Int] = None,
        limit: Option[Int] = None
    ): List[B] =
        val entityManager = videoSequenceDao.entityManager
        val query         = entityManager.createNamedQuery(name)
        namedParameters.foreach { case (a, b) => query.setParameter(a, b) }
        offset.foreach(query.setFirstResult)
        limit.foreach(query.setMaxResults)
        query.getResultList.asScala.toList.map(_.asInstanceOf[B])

    def close(): Unit = videoSequenceDao.close()
