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

package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.repository.VideoDAO
import org.mbari.vampiresquid.repository.jpa.{JPADAOFactory, NotFoundInDatastoreException}

import java.time.{Duration, Instant}
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import org.mbari.vampiresquid.repository.jpa.entity.VideoEntity
import org.mbari.vampiresquid.domain.{Video as VDTO, VideoSequence as VSDTO}

/**
 * @author
 *   Brian Schlining
 * @since 2016-05-26T08:27:00
 */
class VideoController(val daoFactory: JPADAOFactory) extends BaseController:

    private type VDAO = VideoDAO[VideoEntity]

    def findAll(offset: Int, limit: Int)(implicit ec: ExecutionContext): Future[Seq[VDTO]] =
        exec(d => d.findAll(offset, limit).toSeq.map(VDTO.from))

    def findByUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Option[VDTO]] =
        exec(d => d.findByUUID(uuid).map(VDTO.from))

    def findVideoSequenceByVideoUuid(videoUuid: UUID)(implicit
        executionContext: ExecutionContext
    ): Future[Option[VSDTO]] =
        exec(d => d.findByUUID(videoUuid).map(_.getVideoSequence).map(VSDTO.from))

    def findAllNames()(implicit ec: ExecutionContext): Future[Seq[String]] =
        exec(d => d.findAllNames().toSeq)

    def findAllNamesAndTimestamps()(implicit ec: ExecutionContext): Future[Seq[(String, Instant)]] =
        exec(d => d.findAllNamesAndTimestamps().toSeq)

    def findBetweenTimestamps(t0: Instant, t1: Instant)(implicit
        ec: ExecutionContext
    ): Future[Seq[VDTO]] =
        exec(d => d.findBetweenTimestamps(t0, t1).toSeq.map(VDTO.from))

    def findByTimestamp(t0: Instant, window: Duration = Constants.DEFAULT_DURATION_WINDOW)(implicit
        ec: ExecutionContext
    ): Future[Seq[VDTO]] =
        exec(d => d.findByTimestamp(t0, window).toSeq.map(VDTO.from))

    def findByVideoReferenceUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Option[VDTO]] =
        exec(d => d.findByVideoReferenceUUID(uuid).map(VDTO.from))

    def findByVideoSequenceUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Seq[VDTO]] =
        exec(d => d.findByVideoSequenceUUID(uuid).toSeq.map(VDTO.from))

    def findByName(name: String)(implicit ec: ExecutionContext): Future[Option[VDTO]] =
        exec(d => d.findByName(name).map(VDTO.from))

    def findNamesByVideoSequenceName(
        videoSequenceName: String
    )(implicit ec: ExecutionContext): Future[Iterable[String]] =
        exec(d => d.findNamesByVideoSequenceName(videoSequenceName))

    def create(
        videoSequenceUUID: UUID,
        name: String,
        start: Instant,
        duration: Option[Duration] = None,
        description: Option[String] = None
    )(implicit ec: ExecutionContext): Future[VDTO] =
        def fn(dao: VDAO): VDTO =
            dao.findByName(name) match
                case Some(v) => VDTO.from(v)
                case None    =>
                    val vsdao = daoFactory.newVideoSequenceDAO(dao)
                    val vs    = vsdao.findByUUID(videoSequenceUUID)
                    vs match
                        case None                =>
                            throw new NotFoundInDatastoreException(
                                s"No VideoSequence with UUID of $videoSequenceUUID exists"
                            )
                        case Some(videoSequence) =>
                            val video = new VideoEntity(name, start, duration.orNull)
                            description.foreach(video.setDescription)
                            videoSequence.addVideo(video)
                            dao.create(video)
                            VDTO.from(video)
        exec(fn)

    def update(
        uuid: UUID,
        name: Option[String] = None,
        start: Option[Instant] = None,
        duration: Option[Duration] = None,
        description: Option[String] = None,
        videoSequenceUUID: Option[UUID] = None
    )(implicit ec: ExecutionContext): Future[VDTO] =

        def fn(dao: VDAO): VDTO =
            dao.findByUUID(uuid) match
                case None        =>
                    throw new NotFoundInDatastoreException(
                        s"No Video with UUID of $uuid was found in the datastore"
                    )
                case Some(video) =>
                    name.foreach(video.setName)
                    start.foreach(video.setStart)
                    duration.foreach(video.setDuration)
                    description.foreach(video.setDescription)

                    videoSequenceUUID match
                        case None         => VDTO.from(video)
                        case Some(vsUUID) =>
                            val vsDao = daoFactory.newVideoSequenceDAO(dao)
                            vsDao.findByUUID(vsUUID) match
                                case None                =>
                                    throw new NotFoundInDatastoreException(
                                        s"No VideoSequence with UUID of $videoSequenceUUID was found in the datastore"
                                    )
                                case Some(videoSequence) =>
                                    video.getVideoSequence.removeVideo(video)
                                    videoSequence.addVideo(video)
                                    VDTO.from(video)

        exec(fn)

    def delete(uuid: UUID)(implicit ec: ExecutionContext): Future[Boolean] =
        def fn(dao: VDAO): Boolean =
            dao.findByUUID(uuid) match
                case Some(v) =>
                    dao.delete(v)
                    true
                case None    =>
                    false
        exec(fn)

    private def exec[T](fn: VDAO => T)(implicit ec: ExecutionContext): Future[T] =
        val dao = daoFactory.newVideoDAO()
        val f   = dao.runTransaction(fn)
        f.onComplete(t => dao.close())
        f
