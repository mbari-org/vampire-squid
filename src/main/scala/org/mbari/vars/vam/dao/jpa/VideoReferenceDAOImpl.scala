package org.mbari.vars.vam.dao.jpa

import java.net.URI
import java.time.{ Duration, Instant }
import java.util.UUID
import javax.persistence.EntityManager

import org.mbari.vars.vam.dao.VideoReferenceDAO

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-18T13:11:00
 */
class VideoReferenceDAOImpl(entityManager: EntityManager)
    extends BaseDAO[VideoReference](entityManager)
    with VideoReferenceDAO[VideoReference] {

  override def findByVideoUUID(uuid: UUID): Iterable[VideoReference] =
    findByNamedQuery("VideoReference.findByVideoUUID", Map("uuid" -> uuid))

  override def findByURI(uri: URI): Option[VideoReference] =
    findByNamedQuery("VideoReference.findByURI", Map("uri" -> uri))
      .headOption

  override def findAll(): Iterable[VideoReference] =
    findByNamedQuery("VideoReference.findAll")

  override def deleteByUUID(primaryKey: UUID): Unit = {
    val videoReference = findByUUID(primaryKey)
    videoReference.foreach(vr => delete(vr))
  }
}
