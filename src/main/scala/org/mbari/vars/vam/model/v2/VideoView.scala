package org.mbari.vars.vam.model.v2

import java.net.URI
import java.util.UUID

import org.mbari.vars.vam.model.PersistentEntity

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-05T11:10:00
  */

case class VideoView(uuid: UUID,
                     uri: URI,
                     container: String,
                     videoCodec: String,
                     audioCodec: String,
                     width: Int,
                     height: Int,
                     video: Video) extends PersistentEntity