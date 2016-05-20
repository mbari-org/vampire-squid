package org.mbari.vars.vam.dao

import java.net.URI
import java.time.{ Duration, Instant }
import java.util.UUID

import org.mbari.vars.vam.model.v1.VideoView

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T14:51:00
 */
trait VideoReferenceDAO[T <: PersistentObject[UUID]] extends DAO[UUID, T] {

  def findByVideoUUID(uuid: UUID): Iterable[T]
  def findByURI(uri: URI): Option[T]

}
