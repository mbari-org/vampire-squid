package org.mbari.vars.vam.messages

import java.time.Instant
import java.util.UUID

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-19T10:37:00
 */
case class CreateVideoSequence(name: String, cameraID: String = null) extends Msg

case class DeleteVideoSequenceByName(name: String) extends Msg

case class DeleteVideoSequenceByUUID(uuid: UUID) extends Msg

case class FindVideoSequenceByUUID(uuid: UUID) extends Msg

case class FindVideoSequenceByName(name: String) extends Msg

case class FindVideoSequenceByNameAndTimestamp(name: String, timestamp: Instant) extends Msg

case class FindVideoSequenceByCameraIDAndTimestamp(cameraID: String, timestamp: Instant) extends Msg

case class UpdateVideoSequenceByUUID(uuid: UUID, name: String, cameraID: String) extends Msg

case class UpdateVideoSequenceByName(name: String, cameraID: String) extends Msg