package org.mbari.vars.vam.dao

import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future }

/**
 * All DAOs should implement this trait as it defines the minimum CRUD methods needed.
 *
 * @author Brian Schlining
 * @since 2016-05-05T12:44:00
 * @tparam B The type of the entity
 */
trait DAO[B <: PersistentObject] {

  def create(entity: B): Unit
  def update(entity: B): B
  def delete(entity: B): Unit
  def deleteByUUID(primaryKey: UUID): Unit
  def findByUUID(primaryKey: UUID): Option[B]
  def findAll(): Iterable[B]
  def runTransaction[R](fn: this.type => R)(implicit ec: ExecutionContext): Future[R]
  def close(): Unit

}
