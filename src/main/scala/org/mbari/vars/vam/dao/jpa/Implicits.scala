package org.mbari.vars.vam.dao.jpa

import javax.persistence.EntityManager

import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-06T13:34:00
 */
object Implicits {

  private[this] val log = LoggerFactory.getLogger(getClass)

  implicit class RichEntityManager(entityManager: EntityManager) {
    def runTransaction[R](fn: EntityManager => R): Option[R] = {
      val transaction = entityManager.getTransaction
      transaction.begin()
      try {
        val n = fn.apply(entityManager)
        transaction.commit()
        Option(n)
      } catch {
        case (NonFatal(e)) =>
          log.warn("JPA transaction failed", e)
          if (transaction.isActive) {
            transaction.rollback()
          }
          None
      }
    }
  }

}
