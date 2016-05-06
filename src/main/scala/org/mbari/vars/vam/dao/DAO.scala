package org.mbari.vars.vam.dao

import java.util.UUID

import org.mbari.vars.vam.model.PersistentEntity

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-05T12:44:00
  */
trait DAO[T <: PersistentEntity] {

  def create(entity: T): T
  def update(entity: T): T
  def delete(entity: T): Unit
  def findByUUID(uuid: UUID): Option[T]
  def findAll(): Iterable[T]

}
