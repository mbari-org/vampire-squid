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

package org.mbari.vampiresquid

import com.fatboyindustrial.gsonjavatime.Converters
import com.google.gson.reflect.TypeToken
import com.google.gson.{FieldNamingPolicy, GsonBuilder}
import com.typesafe.config.ConfigFactory
import org.mbari.vampiresquid.auth.AuthorizationService
import org.mbari.vampiresquid.etc.gson.{ByteArrayConverter, DurationConverter => GSONDurationConverter}
import org.mbari.vampiresquid.messaging.{MessagingService, NoopMessagingService}
import org.slf4j.LoggerFactory

import java.lang.reflect.Type
import java.time.Duration
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-17T16:03:00
  */
object Constants {

  val CONFIG                  = ConfigFactory.load()
  private[this] val keyWindow = "org.mbari.vars.vam.time.window"
  private[this] val log       = LoggerFactory.getLogger(getClass)

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
    //    val durationType: Type = new TypeToken[Duration]() {}.getType
    //    gsonBuilder.registerTypeAdapter(durationType, new DurationConverter)
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
      log.info(
        s"Failed to find '$keyWindow' in the configuration files (reference.conf or " +
          s"application.conf). Using default window of 120 minutes"
      )
      Duration.ofMinutes(120)
  }

  val AUTH_SERVICE: AuthorizationService = {
    val serviceName = CONFIG.getString("authentication.service")
    log.debug(s"Starting authentication service: $serviceName")
    val clazz = Class.forName(serviceName)
    clazz
      .getDeclaredConstructor()
      .newInstance()
      .asInstanceOf[AuthorizationService]
    //clazz.newInstance().asInstanceOf[AuthorizationService]
  }

  lazy val MESSAGING_SERVICE: MessagingService = {
    val serviceName = CONFIG.getString("messaging.service")
    log.debug(s"Starting messaging service: $serviceName")
    try {
      val clazz = Class.forName(serviceName)
      clazz
        .getDeclaredConstructor()
        .newInstance()
        .asInstanceOf[MessagingService]
      //clazz.newInstance().asInstanceOf[MessagingService]
    }
    catch {
      case NonFatal(e) =>
        log.warn(s"Failed to instantiate messaging service: $serviceName", e)
        new NoopMessagingService
    }
  }

}
