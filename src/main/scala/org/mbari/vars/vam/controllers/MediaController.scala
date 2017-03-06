package org.mbari.vars.vam.controllers

import java.net.URI
import java.time.{ Duration, Instant }
import java.util.{ Arrays => JArrays }

import org.mbari.vars.vam.dao.jpa.{ JPADAOFactory, Video, VideoReference, VideoSequence }
import org.mbari.vars.vam.model.Media

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Convenience API for registering a video
 *
 * @author Brian Schlining
 * @since 2017-03-06T09:20:00
 */
class MediaController(val daoFactory: JPADAOFactory) extends BaseController {

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

          val video = vDao.findByName(videoName) match {
            case Some(v) => v
            case None =>
              vsDao.findByName(videoSequenceName) match {
                case Some(vs) => vs
                case None =>
                  val vs = new VideoSequence
                  vs.name = videoSequenceName
                  vs.cameraID = cameraId
                  vsDao.create(vs)
              }

              val v0 = Video(videoName, start)
              duration.foreach(v0.duration = _)
              vDao.create(v0)
              v0
          }

          video.addVideoReference(vr)
          vr
      }

      Media(videoReference)
    })

  }

}
