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

package org.mbari.vars.vam.model

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import java.net.URI
import java.net.URL
import org.mbari.vars.vam.util.HexUtil
import scala.util.Try
import org.mbari.vars.vam.domain.Media

object CirceCodecs {

  implicit val byteArrayEncoder: Encoder[Array[Byte]] = new Encoder[Array[Byte]] {
    final def apply(xs: Array[Byte]): Json =
      Json.fromString(HexUtil.toHex(xs))
  }
  implicit val byteArrayDecoder: Decoder[Array[Byte]] = Decoder
    .decodeString
    .emapTry(str => Try(HexUtil.fromHex(str)))

  implicit val urlDecoder: Decoder[URL] = Decoder
    .decodeString
    .emapTry(str => Try(URI.create(str).toURL))
  implicit val urlEncoder: Encoder[URL] = Encoder
    .encodeString
    .contramap(_.toString)

  implicit val uriDecoder: Decoder[URI] = Decoder
    .decodeString
    .emapTry(s => Try(URI.create(s)))
  implicit val uriEncoder: Encoder[URI] = Encoder
    .encodeString
    .contramap[URI](_.toString)

  implicit val healthStatusDecoder: Decoder[HealthStatus] = deriveDecoder
  implicit val healthStatusEncoder: Encoder[HealthStatus] = deriveEncoder

  private val printer = Printer.noSpaces.copy(dropNullValues = true)

  /**
   * Convert a circe Json object to a JSON string
   *
   * @param value
   * Any value with an implicit circe coder in scope
   */
  extension (json: Json) def stringify: String = printer.print(json)

  /**
   * Convert an object to a JSON string
   *
   * @param value
   * Any value with an implicit circe coder in scope
   */
  extension[T: Encoder] (value: T) def stringify: String = Encoder[T].apply(value).stringify

  extension[T: Decoder] (jsonString: String) def toJson: Either[ParsingFailure, Json] = parser.parse(jsonString);
  extension[T: Decoder] (jsonString: String) def reify: Either[Error, T] = {
    for
      json <- jsonString.toJson
      result <-  Decoder[T].apply(json.hcursor)
    yield
      result
  }




}
