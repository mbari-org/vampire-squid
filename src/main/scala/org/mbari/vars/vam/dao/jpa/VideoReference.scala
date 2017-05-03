package org.mbari.vars.vam.dao.jpa

import java.net.URI
import javax.activation.MimeType
import javax.persistence.{ EntityListeners, Table, _ }

import com.google.gson.annotations.Expose

import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T18:19:00
 */
@Entity(name = "VideoReference")
@Table(name = "video_references")
@EntityListeners(value = Array(classOf[TransactionLogger]))
@NamedQueries(Array(
  new NamedQuery(
    name = "VideoReference.findAll",
    query = "SELECT v FROM VideoReference v"),
  new NamedQuery(
    name = "VideoReference.findByVideoUUID",
    query = "SELECT v FROM VideoReference v JOIN v.video w WHERE w.uuid = :uuid"),
  new NamedQuery(
    name = "VideoReference.findByURI",
    query = "SELECT v FROM VideoReference v WHERE v.uri = :uri")))
class VideoReference extends HasUUID with HasOptimisticLock with HasDescription {

  @Expose(serialize = true)
  @Basic(optional = false)
  @Index(name = "idx_video_reference_uri", columnList = "uri")
  @Column(
    name = "uri",
    unique = true,
    length = 1024,
    nullable = false)
  @Convert(converter = classOf[URIConverter])
  var uri: URI = _

  /**
   * Defines the video files container. We are using mimetypes to provide
   * container definitions. Note that the mimetype does not always indicate
   * the video/audio encoding
   */
  @Expose(serialize = true)
  @Column(
    name = "container",
    length = 128)
  var container: String = _

  @Expose(serialize = true)
  @Column(
    name = "video_codec",
    length = 128)
  var videoCodec: String = _

  @Expose(serialize = true)
  @Column(
    name = "audio_codec",
    length = 128)
  var audioCodec: String = _

  @Expose(serialize = true)
  @Column(name = "width")
  var width: Int = _

  @Expose(serialize = true)
  @Column(name = "height")
  var height: Int = _

  @Expose(serialize = true)
  @Column(
    name = "frame_rate")
  var frameRate: Double = _

  @Expose(serialize = true)
  @Column(
    name = "size_bytes")
  var size: Long = _

  @Expose(serialize = false)
  @ManyToOne(cascade = Array(CascadeType.PERSIST, CascadeType.DETACH), optional = false)
  @JoinColumn(name = "video_uuid", nullable = false)
  var video: Video = _

  def mimetype: Option[MimeType] = Try(new MimeType(container)).toOption

  // TODO implement storage of SHA512 hash of video files bytes so that we can do reverse lookups using
  // a video file to find it's metadata.

}

object VideoReference {

  def apply(
    uri: URI,
    container: Option[String] = None,
    videoCodec: Option[String],
    audioCodec: Option[String] = None,
    width: Option[Int] = None,
    height: Option[Int] = None,
    frameRate: Option[Double] = None,
    sizeBytes: Option[Long] = None,
    description: Option[String] = None): VideoReference = {
    val videoReference = new VideoReference
    videoReference.uri = uri
    container.foreach(v => videoReference.container = v)
    videoCodec.foreach(v => videoReference.videoCodec = v)
    audioCodec.foreach(v => videoReference.audioCodec = v)
    width.foreach(v => videoReference.width = v)
    height.foreach(v => videoReference.height = v)
    frameRate.foreach(v => videoReference.frameRate = v)
    sizeBytes.foreach(v => videoReference.size = v)
    description.foreach(v => videoReference.description = v)
    videoReference
  }

  def apply(uri: URI): VideoReference = {
    val videoReference = new VideoReference
    videoReference.uri = uri
    videoReference
  }

  def apply(
    uri: URI,
    container: String,
    videoCodec: String,
    audioCodec: String,
    width: Int,
    height: Int): VideoReference = {
    val videoReference = new VideoReference
    videoReference.uri = uri
    videoReference.container = container
    videoReference.videoCodec = videoCodec
    videoReference.audioCodec = audioCodec
    videoReference.width = width
    videoReference.height = height
    videoReference
  }

  def apply(
    uri: URI,
    container: String,
    videoCodec: String,
    audioCodec: String,
    width: Int,
    height: Int,
    description: String): VideoReference = {
    val videoReference = new VideoReference
    videoReference.uri = uri
    videoReference.container = container
    videoReference.videoCodec = videoCodec
    videoReference.audioCodec = audioCodec
    videoReference.width = width
    videoReference.height = height
    videoReference.description = description
    videoReference
  }

  def apply(
    uri: URI,
    container: String,
    videoCodec: String,
    audioCodec: String,
    width: Int,
    height: Int,
    frameRate: Double,
    size: Long): VideoReference = {
    val videoReference = new VideoReference
    videoReference.uri = uri
    videoReference.container = container
    videoReference.videoCodec = videoCodec
    videoReference.audioCodec = audioCodec
    videoReference.width = width
    videoReference.height = height
    videoReference.frameRate = frameRate
    videoReference.size = size
    videoReference
  }

  def apply(
    uri: URI,
    container: String,
    videoCodec: String,
    audioCodec: String,
    width: Int,
    height: Int,
    frameRate: Double,
    size: Long,
    description: String): VideoReference = {
    val videoReference = new VideoReference
    videoReference.uri = uri
    videoReference.container = container
    videoReference.videoCodec = videoCodec
    videoReference.audioCodec = audioCodec
    videoReference.width = width
    videoReference.height = height
    videoReference.frameRate = frameRate
    videoReference.size = size
    videoReference.description = description
    videoReference
  }

}
