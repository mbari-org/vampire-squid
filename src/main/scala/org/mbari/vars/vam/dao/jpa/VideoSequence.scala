package org.mbari.vars.vam.dao.jpa

import javax.persistence._
import java.util.{ ArrayList => JArrayList, List => JList }
import scala.collection.JavaConverters._

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T12:09:00
 */
@Entity(name = "VideoSequence")
@Table(name = "video_sequence")
@EntityListeners(value = Array(classOf[TransactionLogger]))
@NamedQueries(Array(
  new NamedQuery(
    name = "VideoSequence.findAll",
    query = "SELECT v FROM VideoSequence v"
  ),
  new NamedQuery(
    name = "VideoSequence.findByCameraID",
    query = "SELECT v FROM VideoSequence v WHERE v.cameraID = :cameraID"
  ),
  new NamedQuery(
    name = "VideoSequence.findByName",
    query = "SELECT v FROM VideoSequence v WHERE v.name = :name"
  ),
  new NamedQuery(
    name = "VideoSequence.findByVideoUUID",
    query = "SELECT v FROM VideoSequence v WHERE v.uuid = :uuid"
  ),
  new NamedQuery(
    name = "VideoSequence.findBetweenDates",
    query = "SELECT v, w FROM VideoSequence v LEFT JOIN v.javaVideos w WHERE w.startDate BETWEEN :startDate AND :endDate"
  )
))
class VideoSequence extends HasUUID with HasOptimisticLock {

  @Column(
    name = "name",
    nullable = false,
    length = 512,
    unique = true
  )
  var name: String = _

  @Column(
    name = "camera_id",
    nullable = false,
    length = 256
  )
  var cameraID: String = _

  @OneToMany(
    targetEntity = classOf[Video],
    cascade = Array(CascadeType.ALL),
    fetch = FetchType.EAGER,
    mappedBy = "videoSequence"
  )
  private var javaVideos: JList[Video] = new JArrayList[Video]

  def addVideo(video: Video): Unit = {
    javaVideos.add(video)
    video.videoSequence = this
  }
  def removeVideo(video: Video): Unit = {
    javaVideos.remove(video)
    video.videoSequence = null
  }
  def videos: Seq[Video] = javaVideos.asScala

}
