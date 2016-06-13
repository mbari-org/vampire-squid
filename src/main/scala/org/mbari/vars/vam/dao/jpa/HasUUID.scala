package org.mbari.vars.vam.dao.jpa

import java.util.UUID
import javax.persistence._

import com.google.gson.annotations.Expose
import org.mbari.vars.vam.dao.PersistentObject

import scala.util.Try

/**
 * Mixin that supports the UUID fields
 *
 * @author Brian Schlining
 * @since 2016-05-05T17:50:00
 */
trait HasUUID extends PersistentObject {

  @Expose(serialize = true)
  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(
    name = "uuid",
    nullable = false,
    updatable = false)
  @Convert(converter = classOf[UUIDConverter])
  var uuid: UUID = _

  def primaryKey: Option[UUID] = Option(uuid)

}
