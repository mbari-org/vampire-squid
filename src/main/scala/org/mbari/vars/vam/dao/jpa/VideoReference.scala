package org.mbari.vars.vam.dao.jpa

import java.net.URI
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
@Table(name = "video_reference")
@EntityListeners(value = Array(classOf[TransactionLogger]))
class VideoReference extends HasUUID with HasOptimisticLock {

  @Expose(serialize = true)
  @Index(name = "idx_video_reference_uri", columnList = "uri")
  @Column(
    name = "uri",
    unique = true,
    length = 1024,
    nullable = false
  )
  @Convert(converter = classOf[URIConverter])
  var uri: URI = _

  @Expose(serialize = true)
  @Column(
    name = "container",
    length = 128
  )
  var container: String = _

  @Expose(serialize = true)
  @Column(
    name = "video_codec",
    length = 128
  )
  var videoCodec: String = _

  @Expose(serialize = true)
  @Column(
    name = "audio_codec",
    length = 128
  )
  var audioCodec: String = _

  @Expose(serialize = true)
  @Column(name = "width")
  var width: Int = _

  @Expose(serialize = true)
  @Column(name = "height")
  var height: Int = _

  @Expose(serialize = false)
  @ManyToOne(cascade = Array(CascadeType.PERSIST, CascadeType.DETACH))
  @JoinColumn(name = "video_uuid")
  var video: Video = _

}
