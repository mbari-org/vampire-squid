package org.mbari.vars.vam.dao.jpa

import javax.persistence.EntityManagerFactory

import com.typesafe.config.ConfigFactory
import org.eclipse.persistence.config.{ TargetDatabase, TargetServer }
import org.mbari.vars.vam.dao.{ DAOFactory, VideoDAO, VideoReferenceDAO }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-06T11:04:00
 */
object TestDAOFactory extends DAOFactory[VideoSequence, Video, VideoReference] {

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
    val driver = config.getString("org.mbari.vars.vam.database.development.driver")
    val url = config.getString("org.mbari.vars.vam.database.development.url")
    val user = config.getString("org.mbari.vars.vam.database.development.user")
    val password = config.getString("org.mbari.vars.vam.database.development.password")
    EntityManagerFactories(url, user, password, driver, testProps)
  }

  def newVideoSequenceDAO(): VideoSequenceDAOImpl =
    new VideoSequenceDAOImpl(entityManagerFactory.createEntityManager())

  override def newVideoDAO(): VideoDAOImpl = new VideoDAOImpl(entityManagerFactory.createEntityManager())

  override def newVideoViewDAO(): VideoReferenceDAO[VideoReference] = ???
}
