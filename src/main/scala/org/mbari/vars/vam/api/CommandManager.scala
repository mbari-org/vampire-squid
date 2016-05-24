package org.mbari.vars.vam.api

import org.mbari.vars.vam.dao.jpa.{ VideoDAOImpl, VideoReferenceDAOImpl, VideoSequenceDAOImpl }

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-20T14:50:00
 */
class CommandManager(
  videoSequenceDAO: VideoSequenceDAOImpl,
    videoDAO: VideoDAOImpl,
    videoReferenceDAO: VideoReferenceDAOImpl) {

}
