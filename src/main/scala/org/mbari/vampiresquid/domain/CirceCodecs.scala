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

package org.mbari.vampiresquid.domain

import io.circe._
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.mbari.vampiresquid.util.HexUtil
import java.net.URI
import java.net.URL
import scala.util.Try

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
    .emapTry(str => Try(new URL(str)))
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

  def print[T: Encoder](t: T): String = printer.print(t.asJson)

}
