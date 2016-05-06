package org.mbari.vars.vam.dao.jpa

import java.net.URI
import javax.persistence.{ Column, JoinColumn, ManyToOne, Transient }

import scala.util.Try

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T18:19:00
 */
class VideoView extends HasUUID with HasOptimisticLock {

  @Column(
    name = "uri",
    unique = true,
    length = 1024,
    nullable = false
  )
  var uriString: String = _

  def uri: URI = Try(new URI(uriString)).getOrElse(new URI("urn:unknown"))

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

  @ManyToOne
  @JoinColumn(name = "video_uuid")
  var video: Video = _

}
