/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vampiresquid.messaging

import java.time.{Duration, Instant}
import java.util.UUID
import com.google.gson.annotations.Expose
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}
import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.domain.{Media2, VideoReference}
import org.slf4j.LoggerFactory

import java.net.URI
import scala.annotation.meta.field
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.control.NonFatal

/**
  * @author Brian Schlining
  * @since 2017-03-13T16:47:00
  */
class RabbitMQMessagingService extends MessagingService with AutoCloseable {

  private[this] val log         = LoggerFactory.getLogger(getClass)
  private[this] val host        = Constants.CONFIG.getString("rabbitmq.host")
  private[this] val port        = Constants.CONFIG.getInt("rabbitmq.port")
  private[this] val exchange    = Constants.CONFIG.getString("rabbitmq.exchange")
  private[this] val routingKey  = Constants.CONFIG.getString("rabbitmq.routing.key")
  private[this] val username    = Constants.CONFIG.getString("rabbitmq.username")
  private[this] val password    = Constants.CONFIG.getString("rabbitmq.password")
  private[this] val virtualhost = Try(Constants.CONFIG.getString("rabbitmq.virtualhost")).toOption

  private[this] lazy val (connection: Connection, channel: Channel) = {

    //Thread.sleep(30000)
    log.info(s"Connecting to $host:$port (virtual host: $virtualhost) as $username:$password")
    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setPort(port)
    factory.setPassword(password)
    factory.setUsername(username)
    virtualhost.foreach(factory.setVirtualHost)
    val con  = factory.newConnection()
    val chan = con.createChannel()
    chan.exchangeDeclare(exchange, "direct")
    (con, chan)
  }

  /**
    * When a new videoReference is registered it gets passed to this method which will
    * post a notification about it to whatever message broker that you implement.
    *
    * @param videoReference
    */
  override def newVideoReference(
      media: Media2
  )(implicit ec: ExecutionContext): Unit = {

    ec.execute(() => {
      val vm  = NewVideoMessage(media)
      val msg = Constants.GSON.toJson(vm)
      log.debug("Publishing new video message: {}", msg)
      channel.basicPublish(exchange, routingKey, null, msg.getBytes("UTF-8"))
    })

  }

  override def close(): Unit = {
    try {
      if (channel != null) {
        channel.close()
      }
    }
    catch {
      case NonFatal(e) =>
        log.warn("An error occurred when shutting down the MessagingService RabbitMQ Channel", e)
    }

    try {
      if (connection != null) {
        connection.close()
      }
    }
    catch {
      case NonFatal(e) =>
        log.warn("An error occurred when shutting down the MessagingService RabbitMQ connection", e)
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
    @(Expose @field)(serialize = true) uri: URI
)

object NewVideoMessage {
  def apply(m: Media2): NewVideoMessage = {
    new NewVideoMessage(
      m.video_sequence_uuid,
      m.videoSequenceName,
      m.cameraId,
      m.video_uuid,
      m.videoName,
      m.startTimestamp,
      m.duration.orNull,
      m.uri
    )
  }

}
