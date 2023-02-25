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

package org.mbari.vampiresquid.controllers

import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.domain.Media
import org.mbari.vampiresquid.repository.{DAO, VideoReferenceDAO}
import org.mbari.vampiresquid.repository.jpa.{JPADAOFactory, Video, VideoReference, VideoSequence}
import java.net.URI
import java.time.{Duration, Instant}
import java.util.{UUID, Arrays => JArrays}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

/**
  * Convenience API for registering a video
  *
  * @author Brian Schlining
  * @since 2017-03-06T09:20:00
  */
class MediaController(val daoFactory: JPADAOFactory) extends BaseController {

  private[this] val log = LoggerFactory.getLogger(getClass)

  def createMedia(media: Media)(implicit ec: ExecutionContext): Future[Media] = create(
    media.videoSequenceName,
    media.cameraId,
    media.videoName,
    media.uri,
    media.startTimestamp,
    Option(media.duration),
    Option(media.container),
    Option(media.videoCodec),
    Option(media.audioCodec),
    Option(media.width),
    Option(media.height),
    Option(media.frameRate),
    Option(media.sizeBytes),
    Option(media.description),
    Option(media.sha512),
    Option(media.videoSequenceDescription),
    Option(media.videoDescription)
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
  )(implicit ec: ExecutionContext): Future[Media] = {

    val dao = daoFactory.newVideoSequenceDAO()

    dao.runTransaction(vsDao => {
      val vDao  = daoFactory.newVideoDAO(vsDao)
      val vrDao = daoFactory.newVideoReferenceDAO(vsDao)

      var didCreatedVR = false

      val videoReference = vrDao.findByURI(uri) match {
        case Some(vr) =>
          if (sha512.isDefined && !JArrays.equals(vr.sha512, sha512.get)) {
            throw new IllegalArgumentException(
              s"A video with a URI of $uri " +
                s"exists, but it has a different checksum than the one you provided"
            )
          }
          vr
        case None =>
          val vr = VideoReference(
            uri,
            container,
            videoCodec,
            audioCodec,
            width,
            height,
            frameRate,
            sizeBytes,
            videoRefDescription,
            sha512
          )
          didCreatedVR = true
          log.debug("Created {}", vr)

          val video = vDao.findByName(videoName) match {
            // if a duration if provided, make sure it matches the existing duration
            case Some(v) if (
                duration.map(d => d.toMillis().equals(v.duration.toMillis())).getOrElse(true) &&
                start.equals(v.start))=>
              v.addVideoReference(vr)
              v
            case None =>
              val v = Video(videoName, start, duration, List(vr))
              videoDescription.foreach(v.description = _)
              log.debug("Created {}", v)

              vsDao.findByName(videoSequenceName) match {
                case Some(vs) =>
                  vs.addVideo(v)
                  vDao.create(v)
                case None =>
                  val vs = VideoSequence(videoSequenceName, cameraId, List(v))
                  videoSequenceDescription.foreach(vs.description = _)
                  log.debug("Created {}", vs)
                  vsDao.create(vs)
              }
              v
            case _ => 
              throw new IllegalArgumentException(
                s"A video with name $videoName " +
                  s"exists, but it has a different duration and/or start time than the one you provided."
              )
          }

          video.addVideoReference(vr)
          vrDao.create(vr)

          // Notify messaging service of new video reference
          if (didCreatedVR) Constants.MESSAGING_SERVICE.newVideoReference(vr)

          vr
      }

      Media(videoReference)
    })

  }

  def updateMedia(media: Media)(implicit ec: ExecutionContext): Future[Option[Media]] = {
    update(
      media.sha512,
      media.videoSequenceName,
      media.cameraId,
      media.videoName,
      Option(media.uri),
      Option(media.startTimestamp),
      Option(media.duration),
      Option(media.container),
      Option(media.videoCodec),
      Option(media.audioCodec),
      Option(media.width),
      Option(media.height),
      Option(media.frameRate),
      Option(media.sizeBytes),
      Option(media.description),
      Option(media.videoSequenceDescription),
      Option(media.videoDescription)
    )
  }

  /**
    *
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
      findFn: VideoReferenceDAO[VideoReference] => Option[VideoReference],
      videoSequenceName: String,
      cameraId: String,
      videoName: String,
      sha512: Option[Array[Byte]],
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
  )(implicit ec: ExecutionContext): Future[Option[Media]] = {

    val vrDao = daoFactory.newVideoReferenceDAO()
    val vsDao = daoFactory.newVideoSequenceDAO(vrDao)
    val vDao  = daoFactory.newVideoDAO(vrDao)

    def updateVideoReference(): Option[VideoReference] = {
      findFn(vrDao)
        .map(vr => {
          // -- 1. Update VideoReference params
          container.foreach(vr.container = _)
          audioCodec.foreach(vr.audioCodec = _)
          videoCodec.foreach(vr.videoCodec = _)
          width.foreach(vr.width = _)
          height.foreach(vr.height = _)
          frameRate.foreach(vr.frameRate = _)
          sizeBytes.foreach(vr.size = _)
          uri.foreach(vr.uri = _)
          videoRefDescription.foreach(vr.description = _)
          sha512.foreach(vr.sha512 = _)
          vr
        })
    }

    def updateVideoSequence(videoReference: VideoReference): VideoSequence = {
      if (videoReference.video.videoSequence.name != videoSequenceName) {
        val vs = vsDao.findByName(videoSequenceName)
        vs match {
          case None =>
            val vss = new VideoSequence
            vss.name = videoSequenceName
            vss.cameraID = cameraId
            videoSequenceDescription.foreach(vss.description = _)
            vsDao.create(vss)
            vss
          case Some(vss) =>
            log.info(
              s"Changing cameraId from ${vss.cameraID} to $cameraId for VideoSequence ${vss.uuid}"
            )
            vss.cameraID = cameraId
            videoSequenceDescription.foreach(vss.description = _)
            vss
        }
      }
      else {
        videoReference.video.videoSequence.cameraID = cameraId
        videoSequenceDescription.foreach(videoReference.video.videoSequence.description = _)
        videoReference.video.videoSequence
      }
    }

    def updateVideo(videoSequence: VideoSequence, videoReference: VideoReference): Video = {
      if (videoReference.video.name != videoName) {
        val v = vDao.findByName(videoName)
        v match {
          case None =>
            val vv = new Video
            vv.name = videoName
            start.foreach(vv.start = _)
            duration.foreach(vv.duration = _)
            videoSequence.addVideo(vv)
            videoDescription.foreach(vv.description = _)
            // log.debug(s"Creating new video: $vv" )
            // vDao.create(vv)  // Don't need to actually call this. JPA creates this for us
            vv
          case Some(vv) =>
            start.foreach(vv.start = _)
            duration.foreach(vv.duration = _)
            videoDescription.foreach(vv.description = _)
            videoReference.video.removeVideoReference(videoReference)
            vv.addVideoReference(videoReference)
            vv
        }
      }
      else {
        start.foreach(videoReference.video.start = _)
        duration.foreach(videoReference.video.duration = _)
        videoDescription.foreach(videoReference.video.description = _)
        videoReference.video
      }
    }

    val f = vrDao.runTransaction(d => {
      updateVideoReference().map(vr => {
        val vs = updateVideoSequence(vr)
        val v  = updateVideo(vs, vr)
        v.addVideoReference(vr)
        Media(vr)
      })
    })
    f.onComplete(_ => vrDao.close())
    f
  }

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
  )(implicit ec: ExecutionContext): Future[Option[Media]] = {

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

  }

  /**
    * Move a videoReference to a different video under the videoReference's current
    * videoSequence
    *
    * @param videoReferenceUuid
    * @param videoName
    * @param start
    * @param duration
    * @param ec
    * @return
    */
  def moveVideoReference(videoReferenceUuid: UUID, 
    videoName: String,
    start: Instant,
    duration: Duration)(implicit ec: ExecutionContext): Future[Option[Media]] = {

  
    val vrDao = daoFactory.newVideoReferenceDAO()
    val vDao  = daoFactory.newVideoDAO(vrDao)

    val f = vrDao.runTransaction(d => {
    
      d.findByUUID(videoReferenceUuid) match {
        case None => 
          log.debug(s"moveVideoReference: Unable to find videoReference.uuid = ${videoReferenceUuid}")
          None
        case Some(videoReference) =>
          if (videoReference.video.name == videoName) {
            log.debug(s"moveVideoReference: videoReference.uuid = ${videoReferenceUuid} already has video.name = $videoName. No changes made.")
            Some(Media(videoReference))
          }
          else {
            vDao.findByName(videoName) match {
              case None => 
                log.debug(s"moveVideoReference: Creating new video named $videoName for videoReference.uuid = $videoReferenceUuid")
                val oldVideo = videoReference.video
                val videoSequence = oldVideo.videoSequence
                oldVideo.removeVideoReference(videoReference)
                val newVideo = Video(videoName, start, Some(duration), List(videoReference))
                videoSequence.addVideo(newVideo)
                vDao.create(newVideo)
                if (oldVideo.videoReferences.isEmpty) {
                  log.debug(s"moveVideoReference: Deleting empty video named ${oldVideo.name}")
                  vDao.delete(oldVideo)
                }
                Some(Media(videoReference))

              case Some(video) => 
                if (video.duration == duration && video.start == start) {
                  log.debug(s"moveVideoReference: Moving videoReference.uuid = $videoReferenceUuid to existing video.name = $videoName")
                  videoReference.video.removeVideoReference(videoReference)
                  video.addVideoReference(videoReference)
                  Some(Media(videoReference))
                }
                else {
                  log.warn(s"moveVideoReference: videoReference.uuid = $videoReferenceUuid has different start or duration than an existing video.name = $videoName")
                  None
                }
            }
          }
      }
    })
    f.onComplete(_ => vrDao.close())
    f

  }


  def findByVideoReferenceUuid(
      videoReferenceUuid: UUID
  )(implicit ec: ExecutionContext): Future[Option[Media]] = {
    val dao = daoFactory.newVideoReferenceDAO()
    val f = dao.runTransaction(d => {
      d.findByUUID(videoReferenceUuid)
        .map(Media(_))
    })
    f.onComplete(_ => dao.close())
    f
  }

  def findBySha512(sha512: Array[Byte])(implicit ec: ExecutionContext): Future[Option[Media]] = {
    val dao = daoFactory.newVideoReferenceDAO()
    val f = dao.runTransaction(d => {
      d.findBySha512(sha512)
        .map(Media(_))
    })
    f.onComplete(t => dao.close())
    f
  }

  def findByVideoSequenceName(
      name: String
  )(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
    val dao = daoFactory.newVideoSequenceDAO()
    val f = dao.runTransaction(d => {
      d.findByName(name)
        .map(v => v.videoReferences)
        .map(v => v.map(Media(_)))
        .getOrElse(Nil)
    })
    f.onComplete(_ => dao.close())
    f
  }

  def findByVideoSequenceNameAndTimestamp(name: String, ts: Instant)(
      implicit ec: ExecutionContext
  ): Future[Iterable[Media]] =
    findByVideoSequenceName(name)
      .map(ms => ms.filter(m => m.contains(ts)))

  def findByCameraIdAndTimestamp(cameraId: String, ts: Instant)(
      implicit ec: ExecutionContext
  ): Future[Iterable[Media]] = {
    val dao = daoFactory.newVideoSequenceDAO()
    val f = dao.runTransaction(d => {
      d.findByCameraIDAndTimestamp(cameraId, ts)
        .flatMap(vs => vs.videoReferences)
        .map(Media(_))
        .filter(m => m.contains(ts))
    })
    f.onComplete(_ => dao.close())
    f
  }

  def findByCameraIdAndTimestamps(cameraId: String, startTime: Instant, endTime: Instant)(
      implicit ec: ExecutionContext
  ): Future[Iterable[Media]] = {
    val dao = daoFactory.newVideoDAO()
    val f = dao.runTransaction(d => {
      d.findBetweenTimestamps(startTime, endTime)
        .filter(v => v.videoSequence.cameraID == cameraId)
        .flatMap(v => v.videoReferences)
        .map(Media(_))
    })
    f.onComplete(_ => dao.close())
    f
  }

  /**
    * Finds all videoreferences that overlap in time with the provided one. Be aware
    * that the returned media will include the one with the matching UUID.
    * @param videoReferenceUuid The UUID for the videoreference of interest
    * @param ec The execution context
    * @return All videoreferences (as media), in the same video-sequence that
    *         overlap with the videoreference in time. (Returns will include
    *         the videoreference that matches the UUID)
    */
  def findConcurrent(
      videoReferenceUuid: UUID
  )(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
    val dao = daoFactory.newVideoReferenceDAO()
    val f = dao.runTransaction(d =>
      d.findConcurrent(videoReferenceUuid)
        .map(Media(_))
    )
    f.onComplete(_ => dao.close())
    f
  }

  def findByVideoName(name: String)(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
    val dao = daoFactory.newVideoDAO()
    val f = dao.runTransaction(d => {
      d.findByName(name) match {
        case None    => Nil
        case Some(v) => v.videoReferences.map(Media(_))
      }
    })
    f.onComplete(_ => dao.close())
    f
  }

  def findByURI(uri: URI)(implicit ec: ExecutionContext): Future[Option[Media]] = {
    val dao = daoFactory.newVideoReferenceDAO()
    val f   = dao.runTransaction(d => d.findByURI(uri).map(Media(_)))
    f.onComplete(_ => dao.close())
    f
  }

  def findByFileName(filename: String)(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
    val dao = daoFactory.newVideoReferenceDAO()
    val f   = dao.runTransaction(d => d.findByFileName(filename).map(Media(_)))
    f.onComplete(_ => dao.close())
    f
  }

}
