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

import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.repository.VideoReferenceDAO
import org.mbari.vampiresquid.repository.jpa.{JPADAOFactory, NotFoundInDatastoreException}

import java.net.URI
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import org.mbari.vampiresquid.repository.jpa.entity.VideoReferenceEntity
import org.mbari.vampiresquid.domain.{Media, VideoReference as VRDTO}

/**
 * @author
 *   Brian Schlining
 * @since 2016-06-06T16:14:00
 */
class VideoReferenceController(val daoFactory: JPADAOFactory) extends BaseController:

    private type VRDAO = VideoReferenceDAO[VideoReferenceEntity]

    def findAll()(implicit ec: ExecutionContext): Future[Seq[VRDTO]] =
        exec(d => d.findAll().toSeq.map(VRDTO.from))

    def findAllURIs()(implicit ec: ExecutionContext): Future[Seq[URI]] =
        exec(d => d.findAllURIs().toSeq)

    def findByUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Option[VRDTO]] =
        exec(d => d.findByUUID(uuid).map(VRDTO.from))

    def findByVideoUUID(
        videoUUID: UUID
    )(implicit ec: ExecutionContext): Future[Iterable[VRDTO]] =
        exec(d => d.findByVideoUUID(videoUUID).map(VRDTO.from))

    def findByURI(uri: URI)(implicit ec: ExecutionContext): Future[Option[VRDTO]] =
        exec(d => d.findByURI(uri).map(VRDTO.from))

    def findBySha512(
        sha512: Array[Byte]
    )(implicit ec: ExecutionContext): Future[Option[VRDTO]] =
        exec(d => d.findBySha512(sha512).map(VRDTO.from))

    def findConcurrent(uuid: UUID)(implicit ec: ExecutionContext): Future[Iterable[VRDTO]] =
        exec(d => d.findConcurrent(uuid).map(VRDTO.from))

    def create(
        videoUUID: UUID,
        uri: URI,
        container: Option[String] = None,
        videoCodec: Option[String] = None,
        audioCodec: Option[String] = None,
        width: Option[Int] = None,
        height: Option[Int] = None,
        frameRate: Option[Double] = None,
        sizeBytes: Option[Long] = None,
        description: Option[String] = None,
        sha512: Option[Array[Byte]] = None
    )(implicit ec: ExecutionContext): Future[VRDTO] =

        def fn(dao: VRDAO): VRDTO =
            dao.findByURI(uri) match
                case Some(v) => VRDTO.from(v)
                case None    =>
                    val vdao = daoFactory.newVideoDAO(dao)
                    val v    = vdao.findByUUID(videoUUID)
                    v match
                        case None        =>
                            throw new NotFoundInDatastoreException(s"No Video with UUID of $videoUUID exists")
                        case Some(video) =>
                            val videoReference = new VideoReferenceEntity(
                                uri,
                                container.orNull,
                                videoCodec.orNull,
                                audioCodec.orNull,
                                width.map(i => Integer.valueOf(i)).orNull,
                                height.map(i => Integer.valueOf(i)).orNull,
                                frameRate.map(d => java.lang.Double.valueOf(d)).orNull,
                                sizeBytes.map(l => java.lang.Long.valueOf(l)).orNull,
                                description.orNull
                            )
                            video.addVideoReference(videoReference)
                            sha512.foreach(videoReference.setSha512)
                            dao.create(videoReference)
                            val vr             = VRDTO.from(videoReference)
                            val media          = Media.from(videoReference)
                            // Notify messaging service of new video reference
                            // Constants.MESSAGING_SERVICE.newVideoReference(media)
                            vr
        exec(fn)

    def update(
        uuid: UUID,
        videoUUID: Option[UUID],
        uri: Option[URI],
        container: Option[String] = None,
        videoCodec: Option[String],
        audioCodec: Option[String] = None,
        width: Option[Int] = None,
        height: Option[Int] = None,
        frameRate: Option[Double] = None,
        sizeBytes: Option[Long] = None,
        description: Option[String] = None,
        sha512: Option[Array[Byte]] = None
    )(implicit ec: ExecutionContext): Future[VRDTO] =

        def fn(dao: VRDAO): VRDTO =
            dao.findByUUID(uuid) match
                case None                 =>
                    throw new NotFoundInDatastoreException(
                        s"No VideoReference with UUID of $uuid was found in the datastore"
                    )
                case Some(videoReference) =>
                    uri.foreach(videoReference.setUri)
                    container.foreach(videoReference.setContainer)
                    videoCodec.foreach(videoReference.setVideoCodec)
                    audioCodec.foreach(videoReference.setAudioCodec)
                    width.foreach(v => videoReference.setWidth(Integer.valueOf(v)))
                    height.foreach(v => videoReference.setHeight(Integer.valueOf(v)))
                    frameRate.foreach(v => videoReference.setFrameRate(java.lang.Double.valueOf(v)))
                    sizeBytes.foreach(v => videoReference.setSize(java.lang.Long.valueOf(v)))
                    description.foreach(videoReference.setDescription)
                    sha512.foreach(videoReference.setSha512)

                    videoUUID match
                        case None        => VRDTO.from(videoReference)
                        case Some(vUUID) =>
                            val vDao = daoFactory.newVideoDAO(dao)
                            vDao.findByUUID(vUUID) match
                                case None        =>
                                    throw new NotFoundInDatastoreException(s"No Video with UUID of $vUUID was found.")
                                case Some(video) =>
                                    videoReference.getVideo.removeVideoReference(videoReference)
                                    video.addVideoReference(videoReference)
                                    VRDTO.from(videoReference)
        exec(fn)

    def delete(uuid: UUID)(implicit ec: ExecutionContext): Future[Boolean] =
        def fn(dao: VRDAO): Boolean =
            dao.findByUUID(uuid) match
                case Some(v) =>
                    dao.delete(v)
                    true
                case None    =>
                    false
        exec(fn)

    private def exec[T](fn: VRDAO => T)(implicit ec: ExecutionContext): Future[T] =
        val dao = daoFactory.newVideoReferenceDAO()
        val f   = dao.runTransaction(fn)
        f.onComplete(t => dao.close())
        f
