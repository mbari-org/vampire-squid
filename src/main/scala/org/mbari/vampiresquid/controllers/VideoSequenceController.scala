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
import org.mbari.vampiresquid.repository.VideoSequenceDAO
import org.mbari.vampiresquid.repository.jpa.{JPADAOFactory, NotFoundInDatastoreException, VideoSequence}
import java.time.{Duration, Instant}
import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import org.mbari.vampiresquid.repository.jpa.entity.VideoSequenceEntity
import org.mbari.vampiresquid.domain.{VideoSequence => VSDTO}

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-23T11:30:00
  */
class VideoSequenceController(val daoFactory: JPADAOFactory) extends BaseController {

  private type VSDAO = VideoSequenceDAO[VideoSequenceEntity]

  def findAll(implicit ec: ExecutionContext): Future[List[VSDTO]] =
    exec(d => d.findAll().toList.sortBy(_.getName()).map(VSDTO.from))

  def findAllNames(implicit ec: ExecutionContext): Future[Seq[String]] =
    exec(d => d.findAllNames().toSeq.sorted)

  def findAllNamesByCameraID(cameraID: String)(implicit ec: ExecutionContext): Future[Seq[String]] =
    exec(d => d.findAllNamesByCameraID(cameraID).toSeq)

  def findAllCameraIDs(implicit ec: ExecutionContext): Future[Seq[String]] =
    exec(d => d.findAllCameraIDs().toSeq.sorted)

  def findByUUID(uuid: UUID)(implicit ec: ExecutionContext): Future[Option[VSDTO]] =
    exec(d => d.findByUUID(uuid).map(VSDTO.from))

  def findByName(name: String)(implicit ec: ExecutionContext): Future[Option[VSDTO]] =
    exec(d => d.findByName(name).map(VSDTO.from))

  def findByCameraId(id: String)(implicit ec: ExecutionContext): Future[Seq[VSDTO]] =
    exec(d => d.findByCameraID(id).toSeq.sortBy(_.getName()).map(VSDTO.from))

  def findByCameraIDAndTimestamp(
      cameraID: String,
      timestamp: Instant,
      window: Duration = Constants.DEFAULT_DURATION_WINDOW
  )(implicit ec: ExecutionContext): Future[Seq[VSDTO]] =
    exec(d => d.findByCameraIDAndTimestamp(cameraID, timestamp, window).toSeq.map(VSDTO.from))

  def create(name: String, cameraID: String, description: Option[String] = None)(
      implicit ec: ExecutionContext
  ): Future[VSDTO] = {
    def fn(dao: VSDAO): VSDTO = {
      dao.findByName(name) match {
        case Some(vs) => VSDTO.from(vs)
        case None =>
          // val vs = VideoSequence(name, cameraID, description = description)
          val vs = new VideoSequenceEntity(name, cameraID, description.getOrElse(null))
          dao.create(vs)
          VSDTO.from(vs)
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
      description: Option[String] = None
  )(implicit ec: ExecutionContext): Future[VSDTO] = {
    def fn(dao: VSDAO): VSDTO = {
      dao.findByUUID(uuid) match {
        case None =>
          throw new NotFoundInDatastoreException(
            s"No VideoSequence with UUID of $uuid was found in the database"
          )
        case Some(vs) =>
          name.foreach(vs.setName)
          cameraID.foreach(vs.setCameraID)
          description.foreach(vs.setDescription)
          VSDTO.from(vs)
      }
    }
    exec(fn)
  }

  private def exec[T](fn: VSDAO => T)(implicit ec: ExecutionContext): Future[T] = {
    val dao = daoFactory.newVideoSequenceDAO()
    val f   = dao.runTransaction(fn)
    f.onComplete(t => dao.close())
    f
  }

}
