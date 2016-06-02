package org.mbari.vars.vam.dao.jpa

import java.time.{ Duration, Instant }
import java.util.{ ArrayList => JArrayList, List => JList }
import javax.activation
import javax.activation.MimeType
import javax.persistence.{ CascadeType, _ }

import com.google.gson.annotations.{ Expose, SerializedName }

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
@NamedNativeQueries(Array(
  new NamedNativeQuery(
    name = "Video.findAllNames",
    query = "SELECT name FROM video ORDER BY name"),
  new NamedNativeQuery(
    name = "Video.findAllNamesAndStartDates",
    query = "SELECT name, start_time FROM video ORDER BY start_time")))
@NamedQueries(Array(
  new NamedQuery(
    name = "Video.findAll",
    query = "SELECT v FROM Video v"),
  new NamedQuery(
    name = "Video.findByName",
    query = "SELECT v FROM Video v WHERE v.name = :name"),
  new NamedQuery(
    name = "Video.findByUUID",
    query = "SELECT v FROM Video v WHERE v.uuid = :uuid"),
  new NamedQuery(
    name = "Video.findByVideoReferenceUUID",
    query = "SELECT v FROM Video v LEFT JOIN v.javaVideoReferences w WHERE w.uuid = :uuid"),
  new NamedQuery(
    name = "Video.findByVideoSequenceUUID",
    query = "SELECT v FROM Video v JOIN v.videoSequence w WHERE w.uuid = :uuid"),
  new NamedQuery(
    name = "Video.findBetweenDates",
    query = "SELECT v FROM Video v WHERE v.start BETWEEN :startDate AND :endDate")))
class Video extends HasUUID with HasOptimisticLock with HasDescription {

  @Expose(serialize = true)
  @Basic(optional = false)
  @Index(name = "idx_video_name", columnList = "name")
  @Column(
    name = "name",
    nullable = false,
    length = 512,
    unique = true)
  var name: String = _

  @Expose(serialize = true)
  @Basic(optional = false)
  @Index(name = "idx_video_start_time", columnList = "start_time")
  @Column(
    name = "start_time",
    nullable = false)
  @Temporal(value = TemporalType.TIMESTAMP)
  @Convert(converter = classOf[InstantConverter])
  var start: Instant = _

  @Expose(serialize = true)
  @SerializedName(value = "duration_millis")
  @Column(
    name = "duration_millis",
    nullable = true)
  var duration: Duration = _

  @Expose(serialize = false)
  @ManyToOne(cascade = Array(CascadeType.PERSIST, CascadeType.DETACH), optional = false)
  @JoinColumn(name = "video_sequence_uuid", nullable = false)
  var videoSequence: VideoSequence = _

  @Expose(serialize = true)
  @SerializedName(value = "video_references")
  @OneToMany(
    targetEntity = classOf[VideoReference],
    cascade = Array(CascadeType.ALL),
    fetch = FetchType.EAGER,
    mappedBy = "video",
    orphanRemoval = true)
  protected var javaVideoReferences: JList[VideoReference] = new JArrayList[VideoReference]

  def addVideoReference(videoView: VideoReference): Unit = {
    javaVideoReferences.add(videoView)
    videoView.video = this
  }

  def removeVideoReference(videoView: VideoReference): Unit = {
    javaVideoReferences.remove(videoView)
    videoView.video = null
  }

  def videoReferences: Seq[VideoReference] = javaVideoReferences.asScala

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

  def apply(name: String, start: Instant, videoReferences: Iterable[VideoReference]): Video = {
    val v = new Video
    v.name = name
    v.start = start
    videoReferences.foreach(v.addVideoReference)
    v
  }

  def apply(name: String, start: Instant, duration: Duration, videoReferences: Iterable[VideoReference]): Video = {
    val v = new Video
    v.name = name
    v.start = start
    v.duration = duration
    videoReferences.foreach(v.addVideoReference)
    v
  }

}
