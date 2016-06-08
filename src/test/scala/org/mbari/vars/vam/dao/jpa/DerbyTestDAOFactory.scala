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
 * @since 2016-05-06T11:04:00
 */
object DerbyTestDAOFactory extends JPADAOFactory {

  private[this] val config = ConfigFactory.load()
  private[this] val testProps = Map(
    "eclipselink.logging.level" -> "FINE",
    "javax.persistence.database-product-name" -> TargetDatabase.Derby,
    "eclipselink.target-database" -> TargetDatabase.Derby,
    "eclipselink.logging.timestamp" -> "false",
    "eclipselink.logging.session" -> "false",
    "eclipselink.logging.thread" -> "false",
    "javax.persistence.schema-generation.database.action" -> "create",
    "javax.persistence.schema-generation.scripts.action" -> "drop-and-create",
    "javax.persistence.schema-generation.scripts.create-target" -> "target/test-database-create.ddl",
    "javax.persistence.schema-generation.scripts.drop-target" -> "target/test-database-drop.ddl"
  //"eclipselink.ddl-generation" -> "create-tables",
  //"eclipselink.ddl-generation.output-mode" -> "database"
  )

  lazy val entityManagerFactory: EntityManagerFactory = {
    val driver = config.getString("org.mbari.vars.vam.database.derby.driver")
    val url = config.getString("org.mbari.vars.vam.database.derby.url")
    val user = config.getString("org.mbari.vars.vam.database.derby.user")
    val password = config.getString("org.mbari.vars.vam.database.derby.password")
    EntityManagerFactories(url, user, password, driver, testProps)
  }

}
