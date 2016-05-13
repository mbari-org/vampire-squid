package org.mbari.vars.vam.dao.jpa

import java.net.URI
import javax.persistence.{ EntityListeners, Table, _ }

import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T18:19:00
 */
@Entity(name = "VideoView")
@Table(name = "video_view")
@EntityListeners(value = Array(classOf[TransactionLogger]))
class VideoView extends HasUUID with HasOptimisticLock {

  @Index(name = "idx_video_view_uri", columnList = "uri")
  @Column(
    name = "uri",
    unique = true,
    length = 1024,
    nullable = false
  )
  @Convert(converter = classOf[URIConverter])
  var uri: URI = _

  @Column(
    name = "container",
    length = 128
  )
  var container: String = _

  @Column(
    name = "video_codec",
    length = 128
  )
  var videoCodec: String = _

  @Column(
    name = "audio_codec",
    length = 128
  )
  var audioCodec: String = _

  @Column(name = "width")
  var width: Int = _

  @Column(name = "height")
  var height: Int = _

  @ManyToOne(cascade = Array(CascadeType.PERSIST, CascadeType.DETACH))
  @JoinColumn(name = "video_uuid")
  var video: Video = _

}
