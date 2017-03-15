package org.mbari.vars.vam.controllers

import java.net.URI
import java.time.{ Duration, Instant }
import java.util.{ Arrays => JArrays }

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
    f.onComplete(t => dao.close())
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
    f.onComplete(t => dao.close())
    f
  }

}
