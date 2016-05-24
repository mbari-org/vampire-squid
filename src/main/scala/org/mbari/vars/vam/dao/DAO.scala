package org.mbari.vars.vam.dao

import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future }

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
  def deleteByPrimaryKey(primaryKey: A): Unit
  def findByUUID(uuid: UUID): Option[B]
  def findAll(): Iterable[B]
  def runTransaction[R](fn: this.type => R)(implicit ec: ExecutionContext): Future[R]
  def close(): Unit

}
