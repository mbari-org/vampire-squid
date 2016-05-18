package org.mbari.vars.vam.dao.jpa

import java.sql.Timestamp
import java.time.Instant
import javax.persistence.{Column, Convert, Transient, Version}

import com.google.gson.annotations.Expose

import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T16:22:00
 */
trait HasOptimisticLock {

  /** Optimistic lock to prevent concurrent overwrites */
  @Expose(serialize = true)
  @Version
  @Column(name = "last_updated_time")
  protected var lastUpdatedTime: Timestamp = _

  def lastUpdated: Option[Instant] = Option(lastUpdatedTime).map(_.toInstant)

}
