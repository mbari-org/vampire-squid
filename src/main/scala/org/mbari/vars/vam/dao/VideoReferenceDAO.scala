package org.mbari.vars.vam.dao

import java.net.URI
import java.util.UUID

/**
 * Defines the API methods used for accessing VideoReference objects
 *
 * @author Brian Schlining
 * @since 2016-05-05T14:51:00
 */
trait VideoReferenceDAO[T <: PersistentObject] extends DAO[T] {

  def findAll(): Iterable[T]
  def findByVideoUUID(uuid: UUID): Iterable[T]
  def findByURI(uri: URI): Option[T]
  def findBySha512(sha: Array[Byte]): Option[T]

}
