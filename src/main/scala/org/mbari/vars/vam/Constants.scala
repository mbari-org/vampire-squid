package org.mbari.vars.vam

import java.lang.reflect.Type
import java.time.Duration

import com.fatboyindustrial.gsonjavatime.Converters
import com.google.gson.reflect.TypeToken
import com.google.gson.{ FieldNamingPolicy, GsonBuilder }
import com.typesafe.config.ConfigFactory
import org.mbari.vars.vam.auth.AuthorizationService
import org.mbari.vars.vam.json.{ ByteArrayConverter, DurationConverter => GSONDurationConverter }
import org.mbari.vars.vam.messaging.MessagingService
import org.slf4j.LoggerFactory

import scala.util.{ Failure, Success, Try }
/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-17T16:03:00
 */
object Constants {

  val CONFIG = ConfigFactory.load()
  private[this] val keyWindow = "org.mbari.vars.vam.time.window"
  private[this] val log = LoggerFactory.getLogger(getClass)

  /**
   * Gson parser configured for the VAM's use cases.
   */
  val GSON = {

    val gsonBuilder = new GsonBuilder()
      .setPrettyPrinting()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .excludeFieldsWithoutExposeAnnotation()
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    Converters.registerInstant(gsonBuilder)
    val durationType: Type = new TypeToken[Duration]() {}.getType
    gsonBuilder.registerTypeAdapter(durationType, new GSONDurationConverter)
    val byteArrayType: Type = new TypeToken[Array[Byte]]() {}.getType
    gsonBuilder.registerTypeAdapter(byteArrayType, new ByteArrayConverter)
    gsonBuilder.create()

  }

  /**
   * Defines the width of a search window used for searching by date.
   *
   * Reads the 'org.mbari.vars.vam.time.window' property from the config files to configure
   * the window.
   */
  val DEFAULT_DURATION_WINDOW = Try(CONFIG.getDuration(keyWindow)) match {
    case Success(window) => window
    case Failure(e) =>
      log.info(s"Failed to find '$keyWindow' in the configuration files (reference.conf or " +
        s"application.conf). Using default window of 120 minutes")
      Duration.ofMinutes(120)
  }

  val AUTH_SERVICE: AuthorizationService = {
    val serviceName = CONFIG.getString("authentication.service")
    val clazz = Class.forName(serviceName)
    clazz.newInstance().asInstanceOf[AuthorizationService]
  }

  val MESSAGING_SERVICE: MessagingService = {
    val serviceName = CONFIG.getString("messaging.service")
    val clazz = Class.forName(serviceName)
    clazz.newInstance().asInstanceOf[MessagingService]
  }

}
