package org.mbari.vars.vam.dao.jpa

import java.util.UUID
import javax.persistence.EntityManagerFactory

import com.typesafe.config.ConfigFactory
import org.eclipse.persistence.config.TargetDatabase
import org.mbari.vars.vam.dao.{ DAO, VideoDAO, VideoReferenceDAO, VideoSequenceDAO }

import scala.util.Try

/**
 * DAOFactory for creating development database DAOs
 *
 * @author Brian Schlining
 * @since 2016-05-23T15:57:00
 */
object DevelopmentDAOFactory extends JPADAOFactory {

  private[this] val config = ConfigFactory.load()
  private[this] val productName = Try(config.getString("org.mbari.vars.vam.database.development.name")).getOrElse("Auto")
  private[this] val developmentProps = Map(
    "eclipselink.connection-pool.default.initial" -> "2",
    "eclipselink.connection-pool.default.max" -> "16",
    "eclipselink.connection-pool.default.min" -> "2",
    "eclipselink.logging.level" -> "FINE",
    "eclipselink.logging.level" -> "INFO",
    "eclipselink.logging.session" -> "false",
    "eclipselink.logging.thread" -> "false",
    "eclipselink.logging.timestamp" -> "false",
    "eclipselink.target-database" -> productName,
    "javax.persistence.database-product-name" -> productName,
    "javax.persistence.schema-generation.database.action" -> "create")

  lazy val entityManagerFactory: EntityManagerFactory = {
    val driver = config.getString("org.mbari.vars.vam.database.development.driver")
    val url = config.getString("org.mbari.vars.vam.database.development.url")
    val user = config.getString("org.mbari.vars.vam.database.development.user")
    val password = config.getString("org.mbari.vars.vam.database.development.password")
    EntityManagerFactories(url, user, password, driver, developmentProps)
  }

}
