package org.mbari.vars.vam.dao.jpa

import javax.persistence._
import java.util.{ ArrayList => JArrayList, List => JList }

import com.google.gson.annotations.{ Expose, SerializedName }
import org.eclipse.persistence.annotations.CascadeOnDelete

import scala.collection.JavaConverters._

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T12:09:00
 */
@Entity(name = "VideoSequence")
@Table(name = "video_sequences")
@EntityListeners(value = Array(classOf[TransactionLogger]))
@NamedNativeQueries(Array(
  new NamedNativeQuery(
    name = "VideoSequence.findAllNames",
    query = "SELECT name FROM video_sequences ORDER BY name ASC"),
  new NamedNativeQuery(
    name = "VideoSequence.findNamesByCameraID",
    query = "SELECT name FROM video_sequences WHERE camera_id = ?1 ORDER BY name ASC"),
  new NamedNativeQuery(
    name = "VideoSequence.findAllCameraIDs",
    query = "SELECT DISTINCT camera_id FROM video_sequences ORDER BY camera_id ASC")))
@NamedQueries(Array(
  new NamedQuery(
    name = "VideoSequence.findAll",
    query = "SELECT v FROM VideoSequence v"),
  new NamedQuery(
    name = "VideoSequence.findByCameraID",
    query = "SELECT v FROM VideoSequence v WHERE v.cameraID = :cameraID"),
  new NamedQuery(
    name = "VideoSequence.findByName",
    query = "SELECT v FROM VideoSequence v WHERE v.name = :name"),
  new NamedQuery(
    name = "VideoSequence.findByVideoUUID",
    query = "SELECT v FROM VideoSequence v LEFT JOIN v.javaVideos w WHERE w.uuid = :uuid"),
  new NamedQuery(
    name = "VideoSequence.findBetweenDates",
    query = "SELECT v FROM VideoSequence v LEFT JOIN v.javaVideos w WHERE w.start BETWEEN :startDate AND :endDate"),
  new NamedQuery(
    name = "VideoSequence.findByNameAndBetweenDates",
    query = "SELECT v FROM VideoSequence v LEFT JOIN v.javaVideos w WHERE v.name = :name AND w.start BETWEEN :startDate AND :endDate"),
  new NamedQuery(
    name = "VideoSequence.findByCameraIDAndBetweenDates",
    query = "SELECT v FROM VideoSequence v LEFT JOIN v.javaVideos w WHERE v.cameraID = :cameraID AND w.start BETWEEN :startDate AND :endDate")))
class VideoSequence extends HasUUID with HasOptimisticLock with HasDescription {

  @Expose(serialize = true)
  @Basic(optional = false)
  @Index(name = "idx_video_sequences__name", columnList = "name")
  @Column(
    name = "name",
    nullable = false,
    length = 512,
    unique = true)
  var name: String = _

  @Expose(serialize = true)
  @SerializedName(value = "camera_id")
  @Basic(optional = false)
  @Index(name = "idx_video_sequences__camera_id", columnList = "camera_id")
  @Column(
    name = "camera_id",
    nullable = false,
    length = 256)
  var cameraID: String = _

  @Expose(serialize = true)
  @SerializedName(value = "videos")
  @OneToMany(
    targetEntity = classOf[Video],
    cascade = Array(CascadeType.ALL),
    fetch = FetchType.EAGER,
    mappedBy = "videoSequence",
    orphanRemoval = true)
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

  def videoReferences: Seq[VideoReference] = videos.flatMap(_.videoReferences)

  def canEqual(other: Any): Boolean = other.isInstanceOf[VideoSequence]

  override def equals(other: Any): Boolean = other match {
    case that: VideoSequence =>
      (that canEqual this) &&
        name == that.name
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"VideoSequence($name, $cameraID)"
}

object VideoSequence {

  def apply(name: String, cameraID: String, videos: Seq[Video] = Nil, description: Option[String] = None): VideoSequence = {
    val vs = new VideoSequence
    vs.name = name
    vs.cameraID = cameraID
    videos.foreach(vs.addVideo)
    description.foreach(d => vs.description = d)
    vs
  }

}
