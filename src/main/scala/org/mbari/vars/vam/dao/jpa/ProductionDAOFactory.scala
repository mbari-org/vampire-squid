package org.mbari.vars.vam.dao.jpa

import javax.persistence.EntityManagerFactory

import com.typesafe.config.ConfigFactory

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
    "eclipselink.connection-pool.default.initial" -> "2",
    "eclipselink.connection-pool.default.max" -> "16",
    "eclipselink.connection-pool.default.min" -> "2",
    "eclipselink.logging.level" -> "INFO",
    "eclipselink.logging.session" -> "false",
    "eclipselink.logging.thread" -> "false",
    "eclipselink.logging.timestamp" -> "false",
    "eclipselink.target-database" -> productName,
    "javax.persistence.database-product-name" -> productName,
    "javax.persistence.schema-generation.database.action" -> "create")

  lazy val entityManagerFactory: EntityManagerFactory = {
    val driver = config.getString("org.mbari.vars.vam.database.production.driver")
    val url = config.getString("org.mbari.vars.vam.database.production.url")
    val user = config.getString("org.mbari.vars.vam.database.production.user")
    val password = config.getString("org.mbari.vars.vam.database.production.password")
    EntityManagerFactories(url, user, password, driver, productionProps)
  }

}
