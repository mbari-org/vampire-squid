package org.mbari.vars.vam.dao.jpa

import java.sql.Timestamp
import java.time.Instant
import javax.persistence.{Column, Transient, Version}

import scala.util.Try


/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-05T16:22:00
  */
trait HasOptimisticLock {

  /** Optimistic lock to prevent concurrent overwrites */
  @Version
  @Column(name = "last_updated_time")
  private var lastUpdatedTime: Timestamp = _

  def lastUpdate: Instant = Try(lastUpdatedTime.toInstant).getOrElse(Instant.now)
}
