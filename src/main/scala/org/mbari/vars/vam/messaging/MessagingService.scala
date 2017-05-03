package org.mbari.vars.vam.messaging

import org.mbari.vars.vam.dao.jpa.VideoReference

import scala.concurrent.ExecutionContext

/**
 * @author Brian Schlining
 * @since 2017-03-13T16:40:00
 */
trait MessagingService {

  /**
   * When a new videoReference is registered it gets passed to this method which will
   * post a notification about it to whatever message broker that you implement.
   *
   * @param videoReference
   */
  def newVideoReference(videoReference: VideoReference)(implicit ec: ExecutionContext): Unit

}
