package org.mbari.vars.vam.controllers

import java.util.UUID

import org.mbari.vars.vam.dao.VideoSequenceDAO
import org.mbari.vars.vam.dao.jpa.{ JPADAOFactory, VideoSequence }

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

  def findByUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Option[VideoSequence]] =
    exec(d => d.findByUUID(uuid))

  def findByName(name: String)(implicit ec: ExecutionContext): Future[Option[VideoSequence]] =
    exec(d => d.findByName(name))

  def create(name: String, cameraID: String)(implicit ec: ExecutionContext): Future[VideoSequence] = {
    def fn(dao: VSDAO): VideoSequence = {
      dao.findByName(name) match {
        case Some(vs) => vs
        case None =>
          val vs = VideoSequence(name, cameraID)
          dao.create(vs)
          vs
        //dao.findByName(vs.name).get
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

  private def exec[T](fn: VSDAO => T)(implicit ec: ExecutionContext): Future[T] = {
    daoFactory.newVideoSequenceDAO().runTransaction(d => {
      val t = fn(d)
      d.close()
      t
    })
  }

}
