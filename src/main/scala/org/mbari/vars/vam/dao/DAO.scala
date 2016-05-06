package org.mbari.vars.vam.dao

import java.util.UUID

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T12:44:00
 */
trait DAO[A, B <: PersistentObject[A]] {

  def create(entity: B): Unit
  def update(entity: B): B
  def delete(entity: B): Unit
  def findByUUID(uuid: UUID): Option[B]
  def findAll(): Iterable[B]
  def runTransaction[R](fn: () => R): Option[R]

}
