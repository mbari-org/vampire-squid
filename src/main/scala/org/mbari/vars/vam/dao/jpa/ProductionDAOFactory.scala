package org.mbari.vars.vam.dao.jpa

import javax.persistence.EntityManagerFactory

import com.typesafe.config.ConfigFactory
import org.eclipse.persistence.config.TargetDatabase

import scala.util.Try

/**
 * DAOFactory for creating production database DAOs
 *
 * @author Brian Schlining
 * @since 2016-06-08T15:27:00
 */
object ProductionDAOFactory extends JPADAOFactory {

  private[this] val config = ConfigFactory.load()
  private[this] val productName = Try(config.getString("org.mbari.vars.vam.database.production.name")).getOrElse("Auto")
  private[this] val productionProps = Map(
    "eclipselink.logging.level" -> "INFO",
    "javax.persistence.database-product-name" -> productName,
    "eclipselink.target-database" -> productName,
    "eclipselink.logging.timestamp" -> "false",
    "eclipselink.logging.session" -> "false",
    "eclipselink.logging.thread" -> "false",
    "javax.persistence.schema-generation.database.action" -> "create")

  lazy val entityManagerFactory: EntityManagerFactory = {
    val driver = config.getString("org.mbari.vars.vam.database.production.driver")
    val url = config.getString("org.mbari.vars.vam.database.production.url")
    val user = config.getString("org.mbari.vars.vam.database.production.user")
    val password = config.getString("org.mbari.vars.vam.database.production.password")
    EntityManagerFactories(url, user, password, driver, productionProps)
  }

}
