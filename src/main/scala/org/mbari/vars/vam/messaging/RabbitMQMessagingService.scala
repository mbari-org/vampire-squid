package org.mbari.vars.vam.messaging
import java.lang.reflect.Type
import java.time.{ Duration, Instant }
import java.util.UUID

import com.fatboyindustrial.gsonjavatime.Converters
import com.google.gson.annotations.Expose
import com.google.gson.reflect.TypeToken
import com.google.gson.{ FieldNamingPolicy, GsonBuilder }
import com.rabbitmq.client.{ Channel, Connection, ConnectionFactory }
import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.dao.jpa.VideoReference
import org.mbari.vars.vam.json.{ ByteArrayConverter, DurationConverter => GSONDurationConverter }
import org.slf4j.LoggerFactory

import scala.annotation.meta.field
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

/**
 * @author Brian Schlining
 * @since 2017-03-13T16:47:00
 */
class RabbitMQMessagingService extends MessagingService {

  private[this] val host = Constants.CONFIG.getString("rabbitmq.host")
  private[this] val exchange = Constants.CONFIG.getString("rabbitmq.exchange")
  private[this] val routingKey = Constants.CONFIG.getString("rabbitmq.routing.key")

  private[this] val (connection: Connection, channel: Channel) = {
    val factory = new ConnectionFactory
    factory.setHost(host)
    val con = factory.newConnection()
    val chan = con.createChannel()
    chan.exchangeDeclare(exchange, "direct")
    (con, chan)
  }

  private[this] val log = LoggerFactory.getLogger(getClass)

  /**
   * When a new videoReference is registered it gets passed to this method which will
   * post a notification about it to whatever message broker that you implement.
   *
   * @param videoReference
   */
  override def newVideoReference(videoReference: VideoReference)(implicit ec: ExecutionContext): Unit = {

    ec.execute(() => {
      val vm = NewVideoMessage(videoReference)
      val msg = Constants.GSON.toJson(vm)
      log.debug("Publishing new video message: {}", msg)
      channel.basicPublish(exchange, routingKey, null, msg.getBytes("UTF-8"))
    })

  }

  override def finalize(): Unit = {
    try {
      channel.close()
      connection.close()
    } catch {
      case NonFatal(e) => log.warn("An error occurred when shutting down the MessagingService", e)
    }
  }
}

// http://www.piotrbuda.me/2012/10/scala-case-classes-and-annotations-part-1.html
case class NewVideoMessage(
  @(Expose @field)(serialize = true) videoSequenceUuid: UUID,
  @(Expose @field)(serialize = true) videoSequenceName: String,
  @(Expose @field)(serialize = true) cameraId: String,
  @(Expose @field)(serialize = true) videoUuid: UUID,
  @(Expose @field)(serialize = true) videoName: String,
  @(Expose @field)(serialize = true) startTimestamp: Instant,
  @(Expose @field)(serialize = true) durationMillis: Duration,
  @(Expose @field)(serialize = true) videoReference: VideoReference)

object NewVideoMessage {
  def apply(videoReference: VideoReference): NewVideoMessage = {
    val v = videoReference.video
    val vs = v.videoSequence
    NewVideoMessage(vs.uuid, vs.name, vs.cameraID, v.uuid, v.name, v.start, v.duration,
      videoReference)
  }

}
