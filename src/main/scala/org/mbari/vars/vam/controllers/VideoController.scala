package org.mbari.vars.vam.controllers

import java.time.{ Duration, Instant }
import java.util.UUID

import org.mbari.vars.vam.dao.{ VideoDAO }
import org.mbari.vars.vam.dao.jpa._

import scala.concurrent.{ ExecutionContext, Future }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-26T08:27:00
 */
class VideoController(val daoFactory: JPADAOFactory) extends BaseController {

  private type VDAO = VideoDAO[Video]

  def findAllNames(implicit ec: ExecutionContext): Future[Seq[String]] =
    exec(d => d.findAllNames().toSeq)

  def findAllNamesAndTimestamps(implicit ec: ExecutionContext): Future[Seq[(String, Instant)]] =
    exec(d => d.findAllNamesAndTimestamps().toSeq)

  def findBetweenTimestamps(t0: Instant, t1: Instant)(implicit ec: ExecutionContext): Future[Seq[Video]] =
    exec(d => d.findBetweenTimestamps(t0, t1).toSeq)

  def findByTimestamp(t0: Instant, window: Duration = Duration.ofMinutes(15))(implicit ec: ExecutionContext): Future[Seq[Video]] =
    exec(d => d.findByTimestamp(t0, window).toSeq)

  def findByVideoSequenceUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Seq[Video]] =
    exec(d => d.findByVideoSequenceUUID(uuid).toSeq)

  def findByName(name: String)(implicit ec: ExecutionContext): Future[Option[Video]] =
    exec(d => d.findByName(name))

  def create(
    videoSequenceUUID: UUID,
    name: String,
    start: Instant,
    duration: Duration = Duration.ZERO)(implicit ec: ExecutionContext): Future[Video] = {
    def fn(dao: VDAO): Video = {
      dao.findByName(name) match {
        case Some(v) => v
        case None =>
          val vsdao = daoFactory.newVideoSequenceDAO(dao)
          val vs = vsdao.findByUUID(videoSequenceUUID)
          vs match {
            case None =>
              throw new NotFoundInDatastoreException(s"No VideoSequence with UUID of $videoSequenceUUID exists")
            case Some(videoSequence) =>
              val video = Video(name, start, duration)
              videoSequence.addVideo(video)
              dao.create(video)
              video
          }
      }
    }
    exec(fn)
  }

  def update(
    uuid: UUID,
    name: String,
    start: Instant,
    duration: Duration = Duration.ZERO,
    videoSequenceUUID: Option[UUID] = None)(implicit ec: ExecutionContext): Future[Video] = {

    def fn(dao: VDAO): Video = {

      dao.findByUUID(uuid) match {
        case None =>
          throw new NotFoundInDatastoreException(s"No Video with UUID of $uuid was found in the datastore")
        case Some(video) =>
          video.name = name
          video.start = start
          video.duration = duration

          videoSequenceUUID match {
            case None => video
            case Some(vsUUID) =>
              val vsDao = daoFactory.newVideoSequenceDAO(dao)
              vsDao.findByUUID(vsUUID) match {
                case None =>
                  throw new NotFoundInDatastoreException(s"No VideoSequence with UUID of $videoSequenceUUID was found in the datastore")
                case Some(videoSequence) =>
                  video.videoSequence.removeVideo(video)
                  videoSequence.addVideo(video)
                  video
              }

          }
      }
    }
    exec(fn)
  }

  def delete(uuid: UUID)(implicit ec: ExecutionContext): Future[Boolean] = {
    def fn(dao: VDAO): Boolean = {
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

  private def exec[T](fn: VDAO => T)(implicit ec: ExecutionContext): Future[T] = {
    daoFactory.newVideoDAO().runTransaction(d => {
      val t = fn(d)
      d.close()
      t
    })
  }

}
