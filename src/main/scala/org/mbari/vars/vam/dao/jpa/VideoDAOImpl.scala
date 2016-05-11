package org.mbari.vars.vam.dao.jpa

import java.time.Instant
import java.util.UUID
import javax.persistence.EntityManager

import org.mbari.vars.vam.dao.VideoDAO

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-11T14:35:00
  */
class VideoDAOImpl(entityManager: EntityManager)
    extends BaseDAO[UUID, Video](entityManager)
    with VideoDAO[Video] {
  override def findByName(name: String): Option[Video] = ???

  override def findByVideoSequenceUUID(uuid: UUID): Iterable[Video] = ???

  override def findByTimestamp(start: Instant): Iterable[Video] = ???

  override def findAll(): Iterable[Video] = ???

  override def deleteByPrimaryKey(primaryKey: UUID): Unit = ???
}
