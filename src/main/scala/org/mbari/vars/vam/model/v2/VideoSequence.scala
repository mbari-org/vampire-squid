package org.mbari.vars.vam.model.v2

import java.util.UUID

import org.mbari.vars.vam.model.PersistentEntity

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-05T11:09:00
 */
case class VideoSequence(
  uuid: UUID,
  name: String,
  cameraID: String,
  videos: Seq[Video]
) extends PersistentEntity