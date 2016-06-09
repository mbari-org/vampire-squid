package org.mbari.vars.vam.dao.jpa

import java.util.UUID
import javax.persistence.EntityManagerFactory

import com.typesafe.config.ConfigFactory
import org.eclipse.persistence.config.TargetDatabase
import org.mbari.vars.vam.dao.{DAO, VideoDAO, VideoReferenceDAO, VideoSequenceDAO}

import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-23T15:57:00
 */
object DevelopmentDAOFactory extends JPADAOFactory {

  private[this] val config = ConfigFactory.load()
  private[this] val productName = Try(config.getString("org.mbari.vars.vam.database.development.name")).getOrElse("Auto")
  private[this] val developmentProps = Map(
    "eclipselink.logging.level" -> "FINE",
    "javax.persistence.database-product-name" -> productName,
    "eclipselink.target-database" -> productName,
    "eclipselink.logging.timestamp" -> "false",
    "eclipselink.logging.session" -> "false",
    "eclipselink.logging.thread" -> "false",
    "javax.persistence.schema-generation.database.action" -> "create"
  //"eclipselink.ddl-generation" -> "create-tables",
  //"eclipselink.ddl-generation.output-mode" -> "database"
  )

  lazy val entityManagerFactory: EntityManagerFactory = {
    val driver = config.getString("org.mbari.vars.vam.database.development.driver")
    val url = config.getString("org.mbari.vars.vam.database.development.url")
    val user = config.getString("org.mbari.vars.vam.database.development.user")
    val password = config.getString("org.mbari.vars.vam.database.development.password")
    EntityManagerFactories(url, user, password, driver, developmentProps)
  }

}
