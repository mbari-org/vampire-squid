package org.mbari.vars.vam.messaging
import org.mbari.vars.vam.dao.jpa.VideoReference
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
 * @author Brian Schlining
 * @since 2017-03-13T17:06:00
 */
class NoopMessagingService extends MessagingService {

  private[this] val log = LoggerFactory.getLogger(getClass)
  /**
   * When a new videoReference is registered it gets passed to this method which will
   * post a notification about it to whatever message broker that you implement.
   *
   * @param videoReference
   */
  override def newVideoReference(videoReference: VideoReference)(implicit ec: ExecutionContext): Unit = {
    val msg = videoReference.uri.toString
    log.debug("MessagingService: new video url: {}", msg)

  }
}
