package org.mbari.vars.vam.controllers

import java.net.URI
import java.util.UUID

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.dao.VideoReferenceDAO
import org.mbari.vars.vam.dao.jpa._

import scala.concurrent.{ ExecutionContext, Future }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-06-06T16:14:00
 */
class VideoReferenceController(val daoFactory: JPADAOFactory) extends BaseController {

  private type VRDAO = VideoReferenceDAO[VideoReference]

  def findAll(implicit ec: ExecutionContext): Future[Seq[VideoReference]] =
    exec(d => d.findAll().toSeq)

  def findByUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Option[VideoReference]] =
    exec(d => d.findByUUID(uuid))

  def findByVideoUUID(videoUUID: UUID)(implicit ec: ExecutionContext): Future[Iterable[VideoReference]] =
    exec(d => d.findByVideoUUID(videoUUID))

  def findByURI(uri: URI)(implicit ec: ExecutionContext): Future[Option[VideoReference]] =
    exec(d => d.findByURI(uri))

  def findBySha512(sha512: Array[Byte])(implicit ec: ExecutionContext): Future[Option[VideoReference]] =
    exec(d => d.findBySha512(sha512))

  def create(
    videoUUID: UUID,
    uri: URI,
    container: Option[String] = None,
    videoCodec: Option[String],
    audioCodec: Option[String] = None,
    width: Option[Int] = None,
    height: Option[Int] = None,
    frameRate: Option[Double] = None,
    sizeBytes: Option[Long] = None,
    description: Option[String] = None,
    sha512: Option[Array[Byte]] = None)(implicit ec: ExecutionContext): Future[VideoReference] = {

    def fn(dao: VRDAO): VideoReference = {
      dao.findByURI(uri) match {
        case Some(v) => v
        case None =>
          val vdao = daoFactory.newVideoDAO(dao)
          val v = vdao.findByUUID(videoUUID)
          v match {
            case None => throw new NotFoundInDatastoreException(s"No Video with UUID of $videoUUID exists")
            case Some(video) =>
              val videoReference = VideoReference(uri, container, videoCodec, audioCodec, width,
                height, frameRate, sizeBytes, description)
              video.addVideoReference(videoReference)
              sha512.foreach(videoReference.sha512 = _)
              dao.create(videoReference)
              // Notify messaging service of new video reference
              Constants.MESSAGING_SERVICE.newVideoReference(videoReference)
              videoReference
          }
      }
    }
    exec(fn)
  }

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
    sha512: Option[Array[Byte]] = None)(implicit ec: ExecutionContext): Future[VideoReference] = {

    def fn(dao: VRDAO): VideoReference = {
      dao.findByUUID(uuid) match {
        case None => throw new NotFoundInDatastoreException(s"No VideoReference with UUID of $uuid was found in the datastore")
        case Some(videoReference) =>
          uri.foreach(v => videoReference.uri = v)
          container.foreach(v => videoReference.container = v)
          videoCodec.foreach(v => videoReference.videoCodec = v)
          audioCodec.foreach(v => videoReference.audioCodec = v)
          width.foreach(v => videoReference.width = v)
          height.foreach(v => videoReference.height = v)
          frameRate.foreach(v => videoReference.frameRate = v)
          sizeBytes.foreach(v => videoReference.size = v)
          description.foreach(v => videoReference.description = v)
          sha512.foreach(videoReference.sha512 = _)

          videoUUID match {
            case None => videoReference
            case Some(vUUID) =>
              val vDao = daoFactory.newVideoDAO(dao)
              vDao.findByUUID(vUUID) match {
                case None =>
                  throw new NotFoundInDatastoreException(s"No Video with UUID of $vUUID was found.")
                case Some(video) =>
                  videoReference.video.removeVideoReference(videoReference)
                  video.addVideoReference(videoReference)
                  videoReference
              }
          }
      }
    }
    exec(fn)
  }

  def delete(uuid: UUID)(implicit ec: ExecutionContext): Future[Boolean] = {
    def fn(dao: VRDAO): Boolean = {
      dao.findByUUID(uuid) match {
        case Some(v) =>
          dao.delete(v)
          true
        case None =>
          false
      }
    }
    exec(fn)
  }

  private def exec[T](fn: VRDAO => T)(implicit ec: ExecutionContext): Future[T] = {
    val dao = daoFactory.newVideoReferenceDAO()
    val f = dao.runTransaction(fn)
    f.onComplete(t => dao.close())
    f
  }

}
