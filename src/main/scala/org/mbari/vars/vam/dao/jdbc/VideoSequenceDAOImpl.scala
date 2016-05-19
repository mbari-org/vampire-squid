package org.mbari.vars.vam.dao.jdbc

import java.sql.Connection
import java.time.{ Duration, Instant }
import java.util.UUID

import org.mbari.vars.vam.dao.VideoSequenceDAO
import org.mbari.vars.vam.model.v1.VideoSequence

import scala.concurrent.{ ExecutionContext, Future }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T15:03:00
 */
class VideoSequenceDAOImpl(val connection: Connection) extends VideoSequenceDAO[VideoSequence] {

  override def findByCameraID(cameraID: String): Iterable[VideoSequence] = ???

  override def findByName(name: String): Option[VideoSequence] = ???

  override def findByVideoUUID(uuid: UUID): Option[VideoSequence] = ???

  override def findByTimestamp(timestamp: Instant, range: Duration): Iterable[VideoSequence] = ???

  override def findByUUID(uuid: UUID): Option[VideoSequence] = ???

  override def update(entity: VideoSequence): VideoSequence = ???

  override def findAll(): Iterable[VideoSequence] = ???

  override def delete(entity: VideoSequence): Unit = ???

  override def deleteByPrimaryKey(primaryKey: UUID): Unit = ???

  override def create(entity: VideoSequence): Unit = ???

  override def runTransaction[R](fn: (VideoSequenceDAOImpl.this.type) => R)(implicit ec: ExecutionContext): Future[R] = ???

  override def findByNameAndTimestamp(name: String, timestamp: Instant, window: Duration): Iterable[VideoSequence] = ???

  override def findByCameraIDAndTimestamp(cameraID: String, timestamp: Instant, window: Duration): Iterable[VideoSequence] = ???
}
