package org.mbari.vars.vam.dao.jpa

import java.util.UUID
import javax.persistence.EntityManagerFactory

import com.typesafe.config.ConfigFactory
import org.eclipse.persistence.config.TargetDatabase
import org.mbari.vars.vam.dao.{ DAO, VideoDAO, VideoReferenceDAO, VideoSequenceDAO }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-23T15:57:00
 */
object DerbyDAOFactory extends JPADAOFactory {

  private[this] val config = ConfigFactory.load()
  private[this] val developmentProps = Map(
    "eclipselink.logging.level" -> "FINE",
    "javax.persistence.database-product-name" -> TargetDatabase.Derby,
    "eclipselink.target-database" -> TargetDatabase.Derby,
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
