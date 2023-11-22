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

import jakarta.persistence.EntityManagerFactory
import org.mbari.vampiresquid.repository.{DAO, DAOFactory, VideoDAO, VideoReferenceDAO, VideoSequenceDAO}
import org.mbari.vampiresquid.repository.jpa.entity.VideoSequenceEntity
import org.mbari.vampiresquid.repository.jpa.entity.VideoEntity
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity

/**
 * Self-explanatory. THis class creates DAOs for the JPA implementation.
 *
 * @author
 *   Brian Schlining
 * @since 2016-06-08T15:28:00
 */
trait JPADAOFactory extends DAOFactory[VideoSequenceEntity, VideoEntity, VideoReferenceEntity]:

    def entityManagerFactory: EntityManagerFactory

    override def newVideoSequenceDAO(): VideoSequenceDAOImpl =
        new VideoSequenceDAOImpl(entityManagerFactory.createEntityManager())

    override def newVideoDAO(): VideoDAOImpl =
        new VideoDAOImpl(entityManagerFactory.createEntityManager())

    override def newVideoReferenceDAO(): VideoReferenceDAOImpl =
        new VideoReferenceDAOImpl(entityManagerFactory.createEntityManager())

    /**
     * Create a new DAO that share the underlying connection (e.g. EntityManager)
     *
     * @param dao
     * @return
     */
    override def newVideoDAO(dao: DAO[?]): VideoDAO[VideoEntity] =
        new VideoDAOImpl(dao.asInstanceOf[BaseDAO[_]].entityManager)

    /**
     * Create a new DAO that share the underlying connection (e.g. EntityManager)
     *
     * @param dao
     * @return
     */
    override def newVideoSequenceDAO(dao: DAO[?]): VideoSequenceDAO[VideoSequenceEntity] =
        new VideoSequenceDAOImpl(dao.asInstanceOf[BaseDAO[_]].entityManager)

    /**
     * Create a new DAO that share the underlying connection (e.g. EntityManager)
     *
     * @param dao
     * @return
     */
    override def newVideoReferenceDAO(dao: DAO[?]): VideoReferenceDAO[VideoReferenceEntity] =
        new VideoReferenceDAOImpl(dao.asInstanceOf[BaseDAO[_]].entityManager)

class JPADAOFactoryImpl(val entityManagerFactory: EntityManagerFactory) extends JPADAOFactory

object JPADAOFactory extends JPADAOFactory:

    lazy val entityManagerFactory = EntityManagerFactories("database")
