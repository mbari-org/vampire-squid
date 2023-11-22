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

import java.net.URI
import java.time.{Duration, Instant}
import java.util.{Arrays as JArrays, UUID}
import java.{util as ju}
import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.domain.{Media, MutableMedia}
import org.mbari.vampiresquid.etc.circe.CirceCodecs.{*, given}
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.repository.jpa.entity.{VideoEntity, VideoReferenceEntity, VideoSequenceEntity}
import org.mbari.vampiresquid.repository.{DAO, VideoReferenceDAO}
import org.slf4j.LoggerFactory
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*

/**
 * Convenience API for registering a video
 *
 * @author
 *   Brian Schlining
 * @since 2017-03-06T09:20:00
 */
class MediaController(val daoFactory: JPADAOFactory) extends BaseController:

    private[this] val log = LoggerFactory.getLogger(getClass)

    def createMedia(media: Media)(implicit ec: ExecutionContext): Future[Media] =
        require(media.video_sequence_name.isDefined, "videoSequenceName is required")
        require(media.camera_id.isDefined, "cameraId is required")
        require(media.video_name.isDefined, "videoName is required")
        require(media.uri.isDefined, "uri is required")
        require(media.start_timestamp.isDefined, "startTimestamp is required")
        create(
            media.videoSequenceName,
            media.cameraId,
            media.videoName,
            media.uri.get,
            media.startTimestamp,
            media.duration,
            media.container,
            media.videoCodec,
            media.audioCodec,
            media.width,
            media.height,
            media.frameRate,
            media.sizeBytes,
            media.description,
            media.sha512,
            media.videoSequenceDescription,
            media.videoDescription
        )

    def create(
        videoSequenceName: String,
        cameraId: String,
        videoName: String,
        uri: URI,
        start: Instant,
        duration: Option[Duration] = None,
        container: Option[String] = None,
        videoCodec: Option[String] = None,
        audioCodec: Option[String] = None,
        width: Option[Int] = None,
        height: Option[Int] = None,
        frameRate: Option[Double] = None,
        sizeBytes: Option[Long] = None,
        videoRefDescription: Option[String] = None,
        sha512: Option[Array[Byte]] = None,
        videoSequenceDescription: Option[String] = None,
        videoDescription: Option[String] = None
    )(implicit ec: ExecutionContext): Future[Media] =

        val dao = daoFactory.newVideoSequenceDAO()

        dao.runTransaction(vsDao =>
            val vDao  = daoFactory.newVideoDAO(vsDao)
            val vrDao = daoFactory.newVideoReferenceDAO(vsDao)

            var didCreatedVR = false

            val videoReference = vrDao.findByURI(uri) match
                case Some(vr) =>
                    if sha512.isDefined && !JArrays.equals(vr.getSha512, sha512.get) then
                        throw new IllegalArgumentException(
                            s"A video with a URI of $uri " +
                                s"exists, but it has a different checksum than the one you provided"
                        )
                    vr
                case None     =>
                    val vr = new VideoReferenceEntity(
                        uri,
                        container.getOrElse(null),
                        videoCodec.getOrElse(null),
                        audioCodec.getOrElse(null),
                        width.map(Integer.valueOf).getOrElse(null),
                        height.map(Integer.valueOf).getOrElse(null),
                        frameRate.map(java.lang.Double.valueOf).getOrElse(null),
                        sizeBytes.map(java.lang.Long.valueOf).getOrElse(null),
                        videoRefDescription.getOrElse(null),
                        sha512.getOrElse(null)
                    )
                    didCreatedVR = true
                    log.debug("Created {}", vr)

                    val video = vDao.findByName(videoName) match
                        // if a duration if provided, make sure it matches the existing duration
                        case Some(v)
                            if duration.map(d => d.toMillis().equals(v.getDuration.toMillis())).getOrElse(true) &&
                                start.equals(v.getStart()) =>
                            v.addVideoReference(vr)
                            v
                        case None =>
                            val v = new VideoEntity(videoName, start, duration.getOrElse(null), ju.List.of(vr))
                            // val v = Video(videoName, start, duration, List(vr))
                            videoDescription.foreach(v.setDescription)
                            log.debug("Created {}", v)

                            vsDao.findByName(videoSequenceName) match
                                case Some(vs) =>
                                    vs.addVideo(v)
                                    vDao.create(v)
                                case None     =>
                                    val vs = new VideoSequenceEntity(videoSequenceName, cameraId)
                                    vs.addVideo(v)
                                    videoSequenceDescription.foreach(vs.setDescription)
                                    log.debug("Created {}", vs)
                                    vsDao.create(vs)
                            v
                        case _    =>
                            throw new IllegalArgumentException(
                                s"A video with name $videoName " +
                                    s"exists, but it has a different duration and/or start time than the one you provided."
                            )

                    video.addVideoReference(vr)
                    vrDao.create(vr)

                    // Notify messaging service of new video reference
//          if (didCreatedVR) Constants.MESSAGING_SERVICE.newVideoReference(Media.from(vr))

                    vr

            Media.from(videoReference)
        )

    def updateMedia(media: Media)(implicit ec: ExecutionContext): Future[Option[Media]] =
        update(
            media.sha512.orNull,
            media.videoSequenceName,
            media.cameraId,
            media.videoName,
            media.uri,
            Option(media.startTimestamp),
            media.duration,
            media.container,
            media.videoCodec,
            media.audioCodec,
            media.width,
            media.height,
            media.frameRate,
            media.sizeBytes,
            media.description,
            media.videoSequenceDescription,
            media.videoDescription
        )

    def findAndUpdateMedia(
        findFn: VideoReferenceDAO[VideoReferenceEntity] => Option[VideoReferenceEntity],
        media: Media
    )(implicit
        ec: ExecutionContext
    ): Future[Option[Media]] =
        findAndUpdate(
            findFn,
            media.videoSequenceName,
            media.cameraId,
            media.videoName,
            media.sha512,
            media.uri,
            media.start_timestamp,
            media.duration,
            media.container,
            media.videoCodec,
            media.audioCodec,
            media.width,
            media.height,
            media.frameRate,
            media.sizeBytes,
            media.description,
            media.videoSequenceDescription,
            media.videoDescription
        )

    /**
     * @param findFn
     * @param videoSequenceName
     * @param cameraId
     * @param videoName
     * @param sha512
     * @param uri
     * @param start
     * @param duration
     * @param container
     * @param videoCodec
     * @param audioCodec
     * @param width
     * @param height
     * @param frameRate
     * @param sizeBytes
     * @param videoRefDescription
     * @param videoSequenceDescription
     * @param videoDescription
     * @param ec
     * @return
     */
    def findAndUpdate(
        findFn: VideoReferenceDAO[VideoReferenceEntity] => Option[VideoReferenceEntity],
        videoSequenceName: String,
        cameraId: String,
        videoName: String,
        sha512: Option[Array[Byte]] = None,
        uri: Option[URI] = None,
        start: Option[Instant] = None,
        duration: Option[Duration] = None,
        container: Option[String] = None,
        videoCodec: Option[String] = None,
        audioCodec: Option[String] = None,
        width: Option[Int] = None,
        height: Option[Int] = None,
        frameRate: Option[Double] = None,
        sizeBytes: Option[Long] = None,
        videoRefDescription: Option[String] = None,
        videoSequenceDescription: Option[String] = None,
        videoDescription: Option[String] = None
    )(implicit ec: ExecutionContext): Future[Option[Media]] =

        val vrDao = daoFactory.newVideoReferenceDAO()
        val vsDao = daoFactory.newVideoSequenceDAO(vrDao)
        val vDao  = daoFactory.newVideoDAO(vrDao)

        def updateVideoReference(): Option[VideoReferenceEntity] =
            findFn(vrDao)
                .map(vr =>
                    // -- 1. Update VideoReference params
                    container.foreach(vr.setContainer)
                    audioCodec.foreach(vr.setAudioCodec)
                    videoCodec.foreach(vr.setVideoCodec)
                    width.foreach(i => vr.setWidth(i.intValue()))
                    height.foreach(i => vr.setHeight(i.intValue()))
                    frameRate.foreach(i => vr.setFrameRate(i.doubleValue()))
                    sizeBytes.foreach(i => vr.setSize(i.longValue()))
                    uri.foreach(vr.setUri)
                    videoRefDescription.foreach(vr.setDescription)
                    sha512.foreach(vr.setSha512)
                    vr
                )

        def updateVideoSequence(videoReference: VideoReferenceEntity): VideoSequenceEntity =
            if videoReference.getVideo.getVideoSequence.getName != videoSequenceName then
                val vs = vsDao.findByName(videoSequenceName)
                vs match
                    case None      =>
                        val vss = new VideoSequenceEntity
                        vss.setName(videoSequenceName)
                        vss.setCameraID(cameraId)
                        videoSequenceDescription.foreach(vss.setDescription)
                        vsDao.create(vss)
                        vss
                    case Some(vss) =>
                        log.info(
                            s"Changing cameraId from ${vss.getCameraID} to $cameraId for VideoSequence ${vss.getUuid}"
                        )
                        vss.setCameraID(cameraId)
                        videoSequenceDescription.foreach(vss.setDescription)
                        vss
            else
                val vs = videoReference.getVideo.getVideoSequence
                vs.setCameraID(cameraId)
                videoSequenceDescription.foreach(vs.setDescription)
                vs

        def updateVideo(videoSequence: VideoSequenceEntity, videoReference: VideoReferenceEntity): VideoEntity =
            if videoReference.getVideo.getName != videoName then
                val v = vDao.findByName(videoName)
                v match
                    case None     =>
                        val vv = new VideoEntity
                        vv.setName(videoName)
                        if start.isEmpty then
                            throw new RuntimeException(
                                "The update request is moving to a new Video, but no new startTimestamp was provided."
                            )
                        start.foreach(vv.setStart)
                        duration.foreach(vv.setDuration)
                        videoSequence.addVideo(vv)
                        videoDescription.foreach(vv.setDescription)
                        // log.debug(s"Creating new video: $vv" )
                        // vDao.create(vv)  // Don't need to actually call this. JPA creates this for us
                        vv
                    case Some(vv) =>
                        start.foreach(vv.setStart)
                        duration.foreach(vv.setDuration)
                        videoDescription.foreach(vv.setDescription)
                        videoReference.getVideo.removeVideoReference(videoReference)
                        vv.addVideoReference(videoReference)
                        vv
            else
                val v = videoReference.getVideo
                start.foreach(v.setStart)
                duration.foreach(v.setDuration)
                videoDescription.foreach(v.setDescription)
                v

        val f = vrDao.runTransaction(_ =>
            updateVideoReference().map(vr =>
                val vs = updateVideoSequence(vr)
                val v  = updateVideo(vs, vr)
                v.addVideoReference(vr)

                vr
            )
        )
        f.onComplete(_ => vrDao.close())
        f.map(_.map(Media.from))

    /**
     * Mostly this is to deal with video files that have been moved
     * @param sha512
     * @param videoSequenceName
     * @param cameraId
     * @param videoName
     * @param uri
     * @param start
     * @param duration
     * @param container
     * @param videoCodec
     * @param audioCodec
     * @param width
     * @param height
     * @param frameRate
     * @param sizeBytes
     * @param videoRefDescription
     * @param ec
     * @return
     */
    def update(
        sha512: Array[Byte],
        videoSequenceName: String,
        cameraId: String,
        videoName: String,
        uri: Option[URI] = None,
        start: Option[Instant] = None,
        duration: Option[Duration] = None,
        container: Option[String] = None,
        videoCodec: Option[String] = None,
        audioCodec: Option[String] = None,
        width: Option[Int] = None,
        height: Option[Int] = None,
        frameRate: Option[Double] = None,
        sizeBytes: Option[Long] = None,
        videoRefDescription: Option[String] = None,
        videoSequenceDescription: Option[String] = None,
        videoDescription: Option[String] = None
    )(implicit ec: ExecutionContext): Future[Option[Media]] =
        findAndUpdate(
            d => d.findBySha512(sha512),
            videoSequenceName,
            cameraId,
            videoName,
            Option(sha512),
            uri,
            start,
            duration,
            container,
            videoCodec,
            audioCodec,
            width,
            height,
            frameRate,
            sizeBytes,
            videoRefDescription,
            videoSequenceDescription,
            videoDescription
        )

    /**
     * Move a videoReference to a different video under the videoReference's current videoSequence
     *
     * @param videoReferenceUuid
     * @param videoName
     * @param start
     * @param duration
     * @param ec
     * @return
     */
    def moveVideoReference(videoReferenceUuid: UUID, videoName: String, start: Instant, duration: Duration)(implicit
        ec: ExecutionContext
    ): Future[Option[Media]] =

        val vrDao = daoFactory.newVideoReferenceDAO()
        val vDao  = daoFactory.newVideoDAO(vrDao)

        val f = vrDao.runTransaction(d =>
            d.findByUUID(videoReferenceUuid) match
                case None                 =>
                    log.debug(s"moveVideoReference: Unable to find videoReference.uuid = ${videoReferenceUuid}")
                    None
                case Some(videoReference) =>
                    if videoReference.getVideo.getName == videoName then
                        log.debug(
                            s"moveVideoReference: videoReference.uuid = ${videoReferenceUuid} already has video.name = $videoName. No changes made."
                        )
                        Some(Media.from(videoReference))
                    else
                        vDao.findByName(videoName) match
                            case None =>
                                log.debug(
                                    s"moveVideoReference: Creating new video named $videoName for videoReference.uuid = $videoReferenceUuid"
                                )
                                val oldVideo      = videoReference.getVideo
                                val videoSequence = oldVideo.getVideoSequence
                                oldVideo.removeVideoReference(videoReference)
                                val newVideo      = new VideoEntity(videoName, start, duration, ju.List.of(videoReference))
                                videoSequence.addVideo(newVideo)
                                vDao.create(newVideo)
                                if oldVideo.getVideoReferences.isEmpty then
                                    log.debug(s"moveVideoReference: Deleting empty video named ${oldVideo.getName}")
                                    vDao.delete(oldVideo)
                                Some(Media.from(videoReference))

                            case Some(video) =>
                                if video.getDuration == duration && video.getStart == start then
                                    log.debug(
                                        s"moveVideoReference: Moving videoReference.uuid = $videoReferenceUuid to existing video.name = $videoName"
                                    )
                                    videoReference.getVideo.removeVideoReference(videoReference)
                                    video.addVideoReference(videoReference)
                                    Some(Media.from(videoReference))
                                else {
                                    log.warn(
                                        s"moveVideoReference: videoReference.uuid = $videoReferenceUuid has different start or duration than an existing video.name = $videoName"
                                    )
                                    None
                                }
        )
        f.onComplete(_ => vrDao.close())
        f

    def findByVideoReferenceUuid(
        videoReferenceUuid: UUID
    )(implicit ec: ExecutionContext): Future[Option[Media]] =
        val dao = daoFactory.newVideoReferenceDAO()
        val f   = dao.runTransaction(d =>
            d.findByUUID(videoReferenceUuid)
                .map(Media.from(_))
        )
        f.onComplete(_ => dao.close())
        f

    def findBySha512(sha512: Array[Byte])(implicit ec: ExecutionContext): Future[Option[Media]] =
        val dao = daoFactory.newVideoReferenceDAO()
        val f   = dao.runTransaction(d =>
            d.findBySha512(sha512)
                .map(Media.from(_))
        )
        f.onComplete(_ => dao.close())
        f

    def findByVideoSequenceName(
        name: String
    )(implicit ec: ExecutionContext): Future[Iterable[Media]] =
        val dao = daoFactory.newVideoSequenceDAO()
        val f   = dao.runTransaction(d =>
            d.findByName(name)
                .map(v => v.getVideoReferences.asScala)
                .map(v => v.map(Media.from(_)))
                .getOrElse(Nil)
        )
        f.onComplete(_ => dao.close())
        f

    def findByVideoSequenceNameAndTimestamp(name: String, ts: Instant)(implicit
        ec: ExecutionContext
    ): Future[Iterable[Media]] =
        findByVideoSequenceName(name)
            .map(ms => ms.filter(m => m.contains(ts)))

    def findByCameraIdAndTimestamp(cameraId: String, ts: Instant)(implicit
        ec: ExecutionContext
    ): Future[Iterable[Media]] =
        val dao = daoFactory.newVideoSequenceDAO()
        val f   = dao.runTransaction(d =>
            d.findByCameraIDAndTimestamp(cameraId, ts)
                .flatMap(vs => vs.getVideoReferences.asScala)
                .map(Media.from(_))
                .filter(m => m.contains(ts))
        )
        f.onComplete(_ => dao.close())
        f

    def findByCameraIdAndTimestamps(cameraId: String, startTime: Instant, endTime: Instant)(implicit
        ec: ExecutionContext
    ): Future[Iterable[Media]] =
        val dao = daoFactory.newVideoDAO()
        val f   = dao.runTransaction(d =>
            d.findBetweenTimestamps(startTime, endTime)
                .filter(v => v.getVideoSequence.getCameraID == cameraId)
                .flatMap(v => v.getVideoReferences.asScala)
                .map(Media.from(_))
        )
        f.onComplete(_ => dao.close())
        f

    /**
     * Finds all videoreferences that overlap in time with the provided one. Be aware that the returned media will
     * include the one with the matching UUID.
     * @param videoReferenceUuid
     *   The UUID for the videoreference of interest
     * @param ec
     *   The execution context
     * @return
     *   All videoreferences (as media), in the same video-sequence that overlap with the videoreference in time.
     *   (Returns will include the videoreference that matches the UUID)
     */
    def findConcurrent(
        videoReferenceUuid: UUID
    )(implicit ec: ExecutionContext): Future[Iterable[Media]] =
        val dao = daoFactory.newVideoReferenceDAO()
        val f   = dao.runTransaction(d =>
            d.findConcurrent(videoReferenceUuid)
                .map(Media.from(_))
        )
        f.onComplete(_ => dao.close())
        f

    def findByVideoName(name: String)(implicit ec: ExecutionContext): Future[Iterable[Media]] =
        val dao = daoFactory.newVideoDAO()
        val f   = dao.runTransaction(d =>
            d.findByName(name) match
                case None    => Nil
                case Some(v) => v.getVideoReferences.asScala.map(Media.from(_))
        )
        f.onComplete(_ => dao.close())
        f

    def findByURI(uri: URI)(implicit ec: ExecutionContext): Future[Option[Media]] =
        val dao = daoFactory.newVideoReferenceDAO()
        val f   = dao.runTransaction(d => d.findByURI(uri).map(Media.from(_)))
        f.onComplete(_ => dao.close())
        f

    def findByFileName(filename: String)(implicit ec: ExecutionContext): Future[Iterable[Media]] =
        val dao = daoFactory.newVideoReferenceDAO()
        val f   = dao.runTransaction(d => d.findByFileName(filename).map(Media.from(_)))
        f.onComplete(_ => dao.close())
        f
