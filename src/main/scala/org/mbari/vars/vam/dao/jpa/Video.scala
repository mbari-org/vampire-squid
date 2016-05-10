package org.mbari.vars.vam.dao.jpa

import java.sql.Timestamp
import java.time.{Duration, Instant}
import java.util.{Date, ArrayList => JArrayList, List => JList}
import javax.persistence.{CascadeType, _}

import scala.collection.JavaConverters._
import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T17:54:00
 */
@Entity(name = "Video")
@Table(name = "video")
@EntityListeners(value = Array(classOf[TransactionLogger]))
class Video extends HasUUID with HasOptimisticLock {

  @Column(
    name = "name",
    nullable = false,
    length = 512
  )
  var name: String = _

  @Column(
    name = "start_time",
    nullable = false
  )
  @Temporal(value = TemporalType.TIMESTAMP)
  protected var startDate: Date = _

  def start: Instant = Try(startDate.toInstant).getOrElse(Instant.ofEpochSecond(0))

  @Column(
    name = "duration_millis",
    nullable = true
  )
  private var durationMillis: Long = _

  def duration: Duration = Try(Duration.ofMillis(durationMillis)).getOrElse(Duration.ZERO)

  @ManyToOne
  @JoinColumn(name = "video_sequence_uuid")
  var videoSequence: VideoSequence = _

  @OneToMany(
    targetEntity = classOf[VideoView],
    cascade = Array(CascadeType.ALL),
    fetch = FetchType.EAGER,
    mappedBy = "video"
  )
  private var javaVideoViews: JList[VideoView] = new JArrayList[VideoView]

  def addVideoView(videoView: VideoView): Unit = {
    javaVideoViews.add(videoView)
    videoView.video = this
  }
  def removeVideoView(videoView: VideoView): Unit = {
    javaVideoViews.remove(videoView)
    videoView.video = null
  }

  def videoViews: Seq[VideoView] = javaVideoViews.asScala

}

object Video {

  def apply(name: String, start: Instant): Video = {
    val v = new Video
    v.name = name
    v.startDate = Date.from(start)
    v
  }

  def apply(name: String, start: Instant, duration: Duration): Video = {
    val v = new Video
    v.name = name
    v.startDate = Date.from(start)
    v.durationMillis = duration.toMillis
    v
  }

  def apply(name: String, start: Instant, videoViews: Iterable[VideoView]): Video = {
    val v = new Video
    v.name = name
    v.startDate = Date.from(start)
    videoViews.foreach(v.addVideoView)
    v
  }

  def apply(name: String, start: Instant, duration: Duration, videoViews: Iterable[VideoView]): Video = {
    val v = new Video
    v.name = name
    v.startDate = Date.from(start)
    v.durationMillis = duration.toMillis
    videoViews.foreach(v.addVideoView)
    v
  }


}
