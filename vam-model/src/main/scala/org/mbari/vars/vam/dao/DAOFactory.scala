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

  def newVideoDAO(): VideoDAO[B]

  def newVideoReferenceDAO(): VideoReferenceDAO[C]

}
