package org.mbari.vars.vam.dao

import java.util.UUID

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-06T15:39:00
 */
trait DAOFactory[A <: PersistentObject[UUID], B <: PersistentObject[UUID], C <: PersistentObject[UUID]] {

  def newVideoSequenceDAO(): VideoSequenceDAO[A]

  /**
   * Create a new DAO that share the underlying connection (e.g. EntityManager)
   * @param dao
   * @return
   */
  def newVideoSequenceDAO(dao: DAO[UUID, _]): VideoSequenceDAO[A]

  def newVideoDAO(): VideoDAO[B]

  /**
   * Create a new DAO that share the underlying connection (e.g. EntityManager)
   * @param dao
   * @return
   */
  def newVideoDAO(dao: DAO[UUID, _]): VideoDAO[B]

  def newVideoReferenceDAO(): VideoReferenceDAO[C]

  /**
   * Create a new DAO that share the underlying connection (e.g. EntityManager)
   * @param dao
   * @return
   */
  def newVideoReferenceDAO(dao: DAO[UUID, _]): VideoReferenceDAO[C]

}
