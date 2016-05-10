package org.mbari.vars.vam.dao.jpa

import java.time.{ Duration, Instant }
import java.util.{ Date, UUID }
import javax.persistence.EntityManager

import org.mbari.vars.vam.dao.VideoSequenceDAO

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-06T14:20:00
 */

class VideoSequenceDAOImpl(entityManager: EntityManager)
    extends BaseDAO[UUID, VideoSequence](entityManager)
    with VideoSequenceDAO[VideoSequence] {

  override def findByCameraID(cameraID: String): Iterable[VideoSequence] =
    findByNamedQuery("VideoSequence.findByCameraID", Map("cameraID" -> cameraID))

  override def findByName(name: String): Option[VideoSequence] =
    findByNamedQuery("VideoSequence.findByName", Map("name" -> name)).headOption

  override def findByVideoUUID(uuid: UUID): Option[VideoSequence] =
    findByNamedQuery("VideoSequence.findByVideoUUID", Map("uuid" -> uuid.toString.toUpperCase()))
      .headOption

  /**
   *
   * @param timestamp The moment of interest
   * @param window A search window that so that the actual search is timestamp +/- (range / 2)
   * @return
   */
  override def findByTimestamp(timestamp: Instant, window: Duration = Duration.ofMinutes(120)): Iterable[VideoSequence] = {
    val halfRange = window.dividedBy(2)
    val startDate = Date.from(timestamp.minus(halfRange))
    val endDate = Date.from(timestamp.plus(halfRange))
    val videoSequences = findByNamedQuery(
      "VideoSequence.findBetweenDates",
      Map("startDate" -> startDate, "endDate" -> endDate)
    )

    val hasTimestamp = containsTimestamp(_: VideoSequence, timestamp) // Partially apply the function to timestamp

    videoSequences.filter(hasTimestamp)

  }

  override def findByNameAndTimestamp(name: String, timestamp: Instant, window: Duration): Iterable[VideoSequence] = {
    val halfRange = window.dividedBy(2)
    val startDate = Date.from(timestamp.minus(halfRange))
    val endDate = Date.from(timestamp.plus(halfRange))
    val videoSequences = findByNamedQuery(
      "VideoSequence.findByNameAndBetweenDates",
      Map("startDate" -> startDate, "endDate" -> endDate, "name" -> name)
    )

    val hasTimestamp = containsTimestamp(_: VideoSequence, timestamp) // Partially apply the function to timestamp

    videoSequences.filter(hasTimestamp)
  }


  private def containsTimestamp(vs: VideoSequence, timestamp: Instant): Boolean = vs.videos
      .map(v => (v.start, v.start.plus(v.duration)))
      .exists({
        case (a, b) =>
          a.equals(timestamp) ||
              b.equals(timestamp) ||
              (a.isBefore(timestamp) && b.isAfter(timestamp))
      })

  override def findAll(): Iterable[VideoSequence] = ???
}
