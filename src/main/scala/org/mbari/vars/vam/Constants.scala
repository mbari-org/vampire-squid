package org.mbari.vars.vam

import java.lang.reflect.Type
import java.time.Duration

import com.fatboyindustrial.gsonjavatime.Converters
import com.google.gson.reflect.TypeToken
import com.google.gson.{ FieldNamingPolicy, GsonBuilder }
import com.typesafe.config.ConfigFactory
import org.mbari.vars.vam.json.{ DurationConverter => GSONDurationConverter }
import org.slf4j.LoggerFactory

import scala.util.{ Failure, Success, Try }
/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-17T16:03:00
 */
object Constants {

  private[this] val config = ConfigFactory.load()
  private[this] val keyWindow = "org.mbari.vars.vam.time.window"
  private[this] val log = LoggerFactory.getLogger(getClass)

  val GSON = {

    val gsonBuilder = new GsonBuilder()
      .setPrettyPrinting()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .excludeFieldsWithoutExposeAnnotation()
      .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    Converters.registerInstant(gsonBuilder)
    val durationType: Type = new TypeToken[Duration]() {}.getType
    gsonBuilder.registerTypeAdapter(durationType, new GSONDurationConverter)
    gsonBuilder.create()

  }

  val DEFAULT_DURATION_WINDOW = Try(config.getDuration(keyWindow)) match {
    case Success(window) => window
    case Failure(e) =>
      log.info(s"Failed to find '$keyWindow' in the configuration files (reference.conf or " +
        s"application.conf). Using default window of 120 minutes")
      Duration.ofMinutes(120)
  }

}
