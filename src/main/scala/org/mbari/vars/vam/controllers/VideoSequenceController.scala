package org.mbari.vars.vam.controllers

import java.time.{ Duration, Instant }
import java.util.UUID

import org.mbari.vars.vam.dao.VideoSequenceDAO
import org.mbari.vars.vam.dao.jpa.{ JPADAOFactory, NotFoundInDatastoreException, VideoSequence }

import scala.concurrent.{ ExecutionContext, Future }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-23T11:30:00
 */
class VideoSequenceController(val daoFactory: JPADAOFactory) extends BaseController {

  private type VSDAO = VideoSequenceDAO[VideoSequence]

  def findAll(implicit ec: ExecutionContext): Future[List[VideoSequence]] =
    exec(d => d.findAll().toList.sortBy(_.name))

  def findAllNames(implicit ec: ExecutionContext): Future[Seq[String]] =
    exec(d => d.findAllNames().toSeq.sorted)

  def findAllCameraIDs(implicit ec: ExecutionContext): Future[Seq[String]] =
    exec(d => d.findAllCameraIDs().toSeq.sorted)

  def findByUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Option[VideoSequence]] =
    exec(d => d.findByUUID(uuid))

  def findByName(name: String)(implicit ec: ExecutionContext): Future[Option[VideoSequence]] =
    exec(d => d.findByName(name))

  def findByCameraIDAndTimestamp(cameraID: String, timestamp: Instant, window: Duration)(implicit ec: ExecutionContext): Future[Seq[VideoSequence]] =
    exec(d => d.findByCameraIDAndTimestamp(cameraID, timestamp, window).toSeq)

  def create(name: String, cameraID: String, description: Option[String] = None)(implicit ec: ExecutionContext): Future[VideoSequence] = {
    def fn(dao: VSDAO): VideoSequence = {
      dao.findByName(name) match {
        case Some(vs) => vs
        case None =>
          val vs = VideoSequence(name, cameraID)
          dao.create(vs)
          vs
      }
    }
    exec(fn)
  }

  def delete(uuid: UUID)(implicit ec: ExecutionContext): Future[Boolean] = {
    def fn(dao: VSDAO): Boolean = {
      dao.findByUUID(uuid) match {
        case Some(vs) =>
          dao.delete(vs)
          true
        case None =>
          false
      }
    }
    exec(fn)
  }

  def update(
    uuid: UUID,
    name: Option[String] = None,
    cameraID: Option[String] = None,
    description: Option[String] = None)(implicit ec: ExecutionContext): Future[VideoSequence] = {
    def fn(dao: VSDAO): VideoSequence = {
      dao.findByUUID(uuid) match {
        case None =>
          throw new NotFoundInDatastoreException(s"No VideoSequence with UUID of $uuid was found in the database")
        case Some(vs) =>
          name.foreach(n => vs.name = n)
          cameraID.foreach(c => vs.cameraID = c)
          description.foreach(d => vs.description = d)
          vs
      }
    }
    exec(fn)
  }

  private def exec[T](fn: VSDAO => T)(implicit ec: ExecutionContext): Future[T] = {
    val dao = daoFactory.newVideoSequenceDAO()
    val f = dao.runTransaction(fn)
    f.onComplete(t => dao.close())
    f
  }

}
