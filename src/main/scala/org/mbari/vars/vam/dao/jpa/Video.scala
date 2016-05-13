package org.mbari.vars.vam.dao.jpa

import java.time.{ Duration, Instant }
import java.util.{ ArrayList => JArrayList, List => JList }
import javax.persistence.{ CascadeType, _ }

import scala.collection.JavaConverters._

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T17:54:00
 */
@Entity(name = "Video")
@Table(name = "video")
@EntityListeners(value = Array(classOf[TransactionLogger]))
@NamedQueries(Array(
  new NamedQuery(
    name = "Video.findAll",
    query = "SELECT v FROM Video v"
  ),
  new NamedQuery(
    name = "Video.findByName",
    query = "SELECT v FROM Video v WHERE v.name = :name"
  ),
  new NamedQuery(
    name = "Video.findByUUID",
    query = "SELECT v FROM Video v WHERE v.uuid = :uuid"
  ),
  new NamedQuery(
    name = "Video.findByVideoViewUUID",
    query = "SELECT v FROM Video v LEFT JOIN v.javaVideoViews w WHERE w.uuid = :uuid"
  ),
  new NamedQuery(
    name = "Video.findByVideoSequenceUUID",
    query = "SELECT v FROM Video v JOIN v.videoSequence w WHERE w.uuid = :uuid"
  ),
  new NamedQuery(
    name = "Video.findBetweenDates",
    query = "SELECT v FROM Video v WHERE v.start BETWEEN :startDate AND :endDate"
  )
))
class Video extends HasUUID with HasOptimisticLock {

  @Index(name = "idx_video_name", columnList = "name")
  @Column(
    name = "name",
    nullable = false,
    length = 512,
    unique = true
  )
  var name: String = _

  @Index(name = "idx_video_start_time", columnList = "start_time")
  @Column(
    name = "start_time",
    nullable = false
  )
  @Temporal(value = TemporalType.TIMESTAMP)
  @Convert(converter = classOf[InstantConverter])
  var start: Instant = _

  @Column(
    name = "duration_millis",
    nullable = true
  )
  var duration: Duration = _

  @ManyToOne(cascade = Array(CascadeType.PERSIST, CascadeType.DETACH))
  @JoinColumn(name = "video_sequence_uuid", nullable = false)
  var videoSequence: VideoSequence = _

  @OneToMany(
    targetEntity = classOf[VideoView],
    cascade = Array(CascadeType.ALL),
    fetch = FetchType.EAGER,
    mappedBy = "video",
    orphanRemoval = true
  )
  protected var javaVideoViews: JList[VideoView] = new JArrayList[VideoView]

  def addVideoView(videoView: VideoView): Unit = {
    javaVideoViews.add(videoView)
    videoView.video = this
  }
  def removeVideoView(videoView: VideoView): Unit = {
    javaVideoViews.remove(videoView)
    videoView.video = null
  }

  def videoViews: Seq[VideoView] = javaVideoViews.asScala

  override def toString = s"Video($name, $start)"
}

object Video {

  def apply(name: String, start: Instant): Video = {
    val v = new Video
    v.name = name
    v.start = start
    v
  }

  def apply(name: String, start: Instant, duration: Duration): Video = {
    val v = new Video
    v.name = name
    v.start = start
    v.duration = duration
    v
  }

  def apply(name: String, start: Instant, videoViews: Iterable[VideoView]): Video = {
    val v = new Video
    v.name = name
    v.start = start
    videoViews.foreach(v.addVideoView)
    v
  }

  def apply(name: String, start: Instant, duration: Duration, videoViews: Iterable[VideoView]): Video = {
    val v = new Video
    v.name = name
    v.start = start
    v.duration = duration
    videoViews.foreach(v.addVideoView)
    v
  }

}
