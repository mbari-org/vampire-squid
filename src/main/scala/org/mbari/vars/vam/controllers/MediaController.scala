package org.mbari.vars.vam.controllers

import java.net.URI
import java.time.{ Duration, Instant }
import java.util.{ UUID, Arrays => JArrays }

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.dao.jpa.{ JPADAOFactory, Video, VideoReference, VideoSequence }
import org.mbari.vars.vam.model.Media
import org.slf4j.LoggerFactory

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Convenience API for registering a video
 *
 * @author Brian Schlining
 * @since 2017-03-06T09:20:00
 */
class MediaController(val daoFactory: JPADAOFactory) extends BaseController {

  private[this] val log = LoggerFactory.getLogger(getClass)

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
    sha512: Option[Array[Byte]] = None)(implicit ec: ExecutionContext): Future[Media] = {

    val dao = daoFactory.newVideoSequenceDAO()

    dao.runTransaction(vsDao => {
      val vDao = daoFactory.newVideoDAO(vsDao)
      val vrDao = daoFactory.newVideoReferenceDAO(vsDao)

      var didCreatedVR = false

      val videoReference = vrDao.findByURI(uri) match {
        case Some(vr) =>
          if (sha512.isDefined && !JArrays.equals(vr.sha512, sha512.get)) {
            throw new IllegalArgumentException(s"A video with a URI of $uri " +
              s"exists, but it has a different checksum than the one you provided")
          }
          vr
        case None =>
          val vr = VideoReference(uri, container, videoCodec, audioCodec, width, height,
            frameRate, sizeBytes, videoRefDescription, sha512)
          didCreatedVR = true
          log.debug("Created {}", vr)

          val video = vDao.findByName(videoName) match {
            case Some(v) =>
              v.addVideoReference(vr)
              v
            case None =>
              val v = Video(videoName, start, duration, List(vr))
              log.debug("Created {}", v)

              vsDao.findByName(videoSequenceName) match {
                case Some(vs) =>
                  vs.addVideo(v)
                  vDao.create(v)
                  vs
                case None =>
                  val vs = VideoSequence(videoSequenceName, cameraId, List(v))
                  log.debug("Created {}", vs)
                  vsDao.create(vs)
              }

              v
          }

          video.addVideoReference(vr)

          // Notify messaging service of new video reference
          if (didCreatedVR) Constants.MESSAGING_SERVICE.newVideoReference(vr)

          vr
      }

      Media(videoReference)
    })

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
    videoRefDescription: Option[String] = None)(implicit ec: ExecutionContext): Future[Option[Media]] = {

    val vrDao = daoFactory.newVideoReferenceDAO()
    val vsDao = daoFactory.newVideoSequenceDAO(vrDao)
    val vDao = daoFactory.newVideoDAO(vrDao)

    def updateVideoReference(): Option[VideoReference] = vrDao.findBySha512(sha512).map(vr => {
      // -- 1. Update VideoReference params
      container.foreach(vr.container = _)
      audioCodec.foreach(vr.audioCodec = _)
      videoCodec.foreach(vr.videoCodec = _)
      width.foreach(vr.width = _)
      height.foreach(vr.height = _)
      frameRate.foreach(vr.frameRate = _)
      sizeBytes.foreach(vr.size = _)
      uri.foreach(vr.uri = _)
      vr
    })

    def updateVideoSequence(videoReference: VideoReference): VideoSequence = {
      if (videoReference.video.videoSequence.name != videoSequenceName) {
        val vs = vsDao.findByName(videoSequenceName)
        vs match {
          case None =>
            val vss = new VideoSequence
            vss.name = videoSequenceName
            vss.cameraID = cameraId
            vsDao.create(vss)
            vss
          case Some(vss) =>
            log.info(s"Changing cameraId from ${vss.cameraID} to $cameraId for VideoSequence ${vss.uuid}")
            vss.cameraID = cameraId
            vss
        }
      } else {
        videoReference.video.videoSequence.cameraID = cameraId
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
            vv
          case Some(vv) =>
            start.foreach(vv.start = _)
            duration.foreach(vv.duration = _)
            vv.addVideoReference(videoReference)
            vv
        }
      } else {
        start.foreach(videoReference.video.start = _)
        duration.foreach(videoReference.video.duration = _)
        videoReference.video
      }
    }

    val f = vrDao.runTransaction(d => {
      updateVideoReference().map(vr => {
        val vs = updateVideoSequence(vr)
        val v = updateVideo(vs, vr)
        v.addVideoReference(vr)
        Media(vr)
      })
    })
    f.onComplete(_ => vrDao.close())
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

  def findByVideoSequenceName(name: String)(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
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

  def findByVideoSequenceNameAndTimestamp(name: String, ts: Instant)(implicit ec: ExecutionContext): Future[Iterable[Media]] =
    findByVideoSequenceName(name)
      .map(ms => ms.filter(m => m.contains(ts)))

  def findByCameraIdAndTimestamp(cameraId: String, ts: Instant)(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
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

  def findByCameraIdAndTimestamps(cameraId: String, startTime: Instant, endTime: Instant)(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
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
  def findConcurrent(videoReferenceUuid: UUID)(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
    val dao = daoFactory.newVideoReferenceDAO()
    val f = dao.runTransaction(d =>
      d.findConcurrent(videoReferenceUuid)
        .map(Media(_)))
    f.onComplete(_ => dao.close())
    f
  }

  def findByVideoName(name: String)(implicit ec: ExecutionContext): Future[Iterable[Media]] = {
    val dao = daoFactory.newVideoDAO()
    val f = dao.runTransaction(d => {
      d.findByName(name) match {
        case None => Nil
        case Some(v) => v.videoReferences.map(Media(_))
      }
    })
    f.onComplete(_ => dao.close())
    f
  }

  def findByURI(uri: URI)(implicit ec: ExecutionContext): Future[Option[Media]] = {
    val dao = daoFactory.newVideoReferenceDAO()
    val f = dao.runTransaction(d => d.findByURI(uri).map(Media(_)))
    f.onComplete(_ => dao.close())
    f
  }

}
