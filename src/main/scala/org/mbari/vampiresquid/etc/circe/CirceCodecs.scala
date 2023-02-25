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

package org.mbari.vampiresquid.etc.circe

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._
import org.mbari.vampiresquid.domain.{HealthStatus, LastUpdatedTime, Video, VideoReference, VideoSequence}
import org.mbari.vampiresquid.util.HexUtil

import java.net.{URI, URL}
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

  implicit val lastUpdatedTimeDecoder: Decoder[LastUpdatedTime] = deriveDecoder
  implicit val lastUpdatedTimeEncoder: Encoder[LastUpdatedTime] = deriveEncoder

  implicit val healthStatusDecoder: Decoder[HealthStatus] = deriveDecoder
  implicit val healthStatusEncoder: Encoder[HealthStatus] = deriveEncoder

  implicit val videoReferenceDecoder: Decoder[VideoReference] = deriveDecoder
  implicit val videoReferenceEncoder: Encoder[VideoReference] = deriveEncoder

  implicit val videoDecoder: Decoder[Video] = deriveDecoder
  implicit val videoEncoder: Encoder[Video] = deriveEncoder

  implicit val videoSequenceDecoder: Decoder[VideoSequence] = deriveDecoder
  implicit val videoSequenceEncoder: Encoder[VideoSequence] = deriveEncoder

  private val printer = Printer.noSpaces.copy(dropNullValues = true)

  def print[T: Encoder](t: T): String = printer.print(t.asJson)

}
