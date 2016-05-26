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
 * @since 2016-05-19T16:31:00
 */
object H2TestDAOFactory extends JPADAOFactory {

  private[this] val config = ConfigFactory.load()
  private[this] val testProps = Map(
    "eclipselink.logging.level" -> "FINE",
    "javax.persistence.database-product-name" -> TargetDatabase.Database,
    "eclipselink.target-database" -> TargetDatabase.Database,
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
    val driver = config.getString("org.mbari.vars.vam.database.h2.driver")
    val url = config.getString("org.mbari.vars.vam.database.h2.url")
    val user = config.getString("org.mbari.vars.vam.database.h2.user")
    val password = config.getString("org.mbari.vars.vam.database.h2.password")
    EntityManagerFactories(url, user, password, driver, testProps)
  }

  def newVideoSequenceDAO(): VideoSequenceDAOImpl =
    new VideoSequenceDAOImpl(entityManagerFactory.createEntityManager())

  override def newVideoDAO(): VideoDAOImpl = new VideoDAOImpl(entityManagerFactory.createEntityManager())

  override def newVideoReferenceDAO(): VideoReferenceDAO[VideoReference] = new VideoReferenceDAOImpl(entityManagerFactory.createEntityManager())

  /**
   * Create a new DAO that share the underlying connection (e.g. EntityManager)
   *
   * @param dao
   * @return
   */
  override def newVideoDAO(dao: DAO[UUID, _]): VideoDAO[Video] =
    new VideoDAOImpl(dao.asInstanceOf[BaseDAO[UUID, _]].entityManager)

  /**
   * Create a new DAO that share the underlying connection (e.g. EntityManager)
   *
   * @param dao
   * @return
   */
  override def newVideoSequenceDAO(dao: DAO[UUID, _]): VideoSequenceDAO[VideoSequence] =
    new VideoSequenceDAOImpl(dao.asInstanceOf[BaseDAO[UUID, _]].entityManager)

  /**
   * Create a new DAO that share the underlying connection (e.g. EntityManager)
   *
   * @param dao
   * @return
   */
  override def newVideoReferenceDAO(dao: DAO[UUID, _]): VideoReferenceDAO[VideoReference] =
    new VideoReferenceDAOImpl(dao.asInstanceOf[BaseDAO[UUID, _]].entityManager)
}