package org.mbari.vars.vam.messages

import java.lang.reflect.Type
import java.time.Duration

import com.fatboyindustrial.gsonjavatime.Converters
import com.google.gson.reflect.TypeToken
import com.google.gson.{ FieldNamingPolicy, GsonBuilder }
import org.mbari.vars.vam.json.{ DurationConverter => GSONDurationConverter }
/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-19T14:00:00
 */
object Constants {

  val GSON = {

    val gsonBuilder = new GsonBuilder()
      .setPrettyPrinting()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)

    Converters.registerInstant(gsonBuilder)
    val durationType: Type = new TypeToken[Duration]() {}.getType
    gsonBuilder.registerTypeAdapter(durationType, new GSONDurationConverter)
    gsonBuilder.create()

  }

}
