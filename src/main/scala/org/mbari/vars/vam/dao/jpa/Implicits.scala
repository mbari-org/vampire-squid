package org.mbari.vars.vam.dao.jpa

import javax.persistence.EntityManager

import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
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
    def runTransaction[R](fn: EntityManager => R)(implicit ec: ExecutionContext): Future[R] = {
      Future {
        val transaction = entityManager.getTransaction
        transaction.begin()
        try {
          val n = fn.apply(entityManager)
          transaction.commit()
          n
        }
        finally {
          if (transaction.isActive) {
            transaction.rollback()
          }
        }
      }
    }
  }

}
