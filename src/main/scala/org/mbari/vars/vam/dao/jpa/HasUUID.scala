package org.mbari.vars.vam.dao.jpa

import java.util.UUID
import javax.persistence.{ Column, GeneratedValue, Id, Transient }

import org.mbari.vars.vam.dao.PersistentObject

import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T17:50:00
 */
trait HasUUID extends PersistentObject[UUID] {

  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(
    name = "uuid",
    nullable = false,
    updatable = false
  )
  private var uuid: String = _

  def primaryKey: Option[UUID] = Try(UUID.fromString(uuid)).toOption

}
