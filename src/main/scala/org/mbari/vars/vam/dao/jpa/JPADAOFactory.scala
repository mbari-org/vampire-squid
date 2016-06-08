package org.mbari.vars.vam.dao.jpa

import java.util.UUID
import javax.persistence.EntityManagerFactory

import org.mbari.vars.vam.dao._

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-06-08T15:28:00
 */
trait JPADAOFactory extends DAOFactory[VideoSequence, Video, VideoReference] {

  def entityManagerFactory: EntityManagerFactory

  override def newVideoSequenceDAO(): VideoSequenceDAOImpl =
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
