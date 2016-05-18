package org.mbari.vars.vam.dao.jpa

import java.time.{ Duration, Instant }
import java.util.{ Date, UUID }
import javax.persistence.EntityManager

import org.mbari.vars.vam.dao.VideoDAO

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-11T14:35:00
 */
class VideoDAOImpl(entityManager: EntityManager)
    extends BaseDAO[UUID, Video](entityManager)
    with VideoDAO[Video] {
  override def findByName(name: String): Option[Video] =
    findByNamedQuery("Video.findByName", Map("name" -> name)).headOption

  override def findByVideoSequenceUUID(uuid: UUID): Iterable[Video] =
    findByNamedQuery("Video.findByVideoSequenceUUID", Map("uuid" -> uuid))

  override def findByVideoReferenceUUID(uuid: UUID): Option[Video] =
    findByNamedQuery("Video.findByVideoReferenceUUID", Map("uuid" -> uuid))
      .headOption

  override def findByTimestamp(timestamp: Instant, window: Duration = Duration.ofMinutes(120)): Iterable[Video] = {
    val halfRange = window.dividedBy(2)
    val startDate = timestamp.minus(halfRange)
    val endDate = timestamp.plus(halfRange)
    val videos = findByNamedQuery("Video.findBetweenDates", Map("startDate" -> startDate, "endDate" -> endDate))
    val hasTimestamp = containsTimestamp(_: Video, timestamp)
    videos.filter(hasTimestamp)
  }

  override def findAll(): Iterable[Video] = findByNamedQuery("Video.findAll")

  override def deleteByPrimaryKey(primaryKey: UUID): Unit = {
    val video = findByPrimaryKey(primaryKey)
    video.foreach(v => delete(v))
  }

  private def containsTimestamp(video: Video, timestamp: Instant): Boolean = {
    val startDate = video.start
    val endDate = video.start.plus(video.duration)
    startDate.equals(timestamp) ||
      endDate.equals(timestamp) ||
      startDate.isBefore(timestamp) && endDate.isAfter(timestamp)
  }
}
