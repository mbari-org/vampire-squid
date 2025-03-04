/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
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

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import org.mbari.vampiresquid.domain.{
    Authorization,
    BadRequest,
    ErrorMsg,
    HealthStatus,
    LastUpdatedTime,
    Media,
    NotFound,
    ServerError,
    ServiceStatus,
    StatusMsg,
    Unauthorized,
    Video,
    VideoReference,
    VideoSequence,
    VideoSequenceCreate,
    VideoSequenceUpdate
}
import java.net.{URI, URL}
import java.util.HexFormat
import scala.util.Try
import java.time.Duration
import org.mbari.vampiresquid.domain.VideoReferenceCreate
import org.mbari.vampiresquid.domain.VideoReferenceUpdate
import org.mbari.vampiresquid.domain.VideoUpdate

object CirceCodecs:

    private val hex = HexFormat.of()

    given Encoder[Array[Byte]] = new Encoder[Array[Byte]]:
        final def apply(xs: Array[Byte]): Json =
            Json.fromString(hex.formatHex(xs))
    given Decoder[Array[Byte]] = Decoder
        .decodeString
        .emapTry(str => Try(hex.parseHex(str)))

    given Decoder[URL] = Decoder
        .decodeString
        .emapTry(str => Try(URI.create(str).toURL()))
    given Encoder[URL] = Encoder
        .encodeString
        .contramap(_.toString)

    given uriDecoder: Decoder[URI] = Decoder
        .decodeString
        .emapTry(s => Try(URI.create(s)))
    given uriEncoder: Encoder[URI] = Encoder
        .encodeString
        .contramap[URI](_.toString)

    given Decoder[Duration] = Decoder
        .decodeLong
        .emapTry(lng => Try(Duration.ofMillis(lng)))
    given Encoder[Duration] = Encoder
        .encodeLong
        .contramap(_.toMillis)

    given Decoder[Media] = deriveDecoder
    given Encoder[Media] = deriveEncoder

    given Decoder[Authorization] = deriveDecoder
    given Encoder[Authorization] = deriveEncoder

    given Decoder[ErrorMsg] = deriveDecoder
    given Encoder[ErrorMsg] = deriveEncoder

    given Decoder[BadRequest] = deriveDecoder
    given Encoder[BadRequest] = deriveEncoder

    given Decoder[HealthStatus] = deriveDecoder
    given Encoder[HealthStatus] = deriveEncoder

    given Decoder[ServiceStatus] = deriveDecoder
    given Encoder[ServiceStatus] = deriveEncoder

    given Decoder[StatusMsg] = deriveDecoder
    given Encoder[StatusMsg] = deriveEncoder

    given Decoder[NotFound] = deriveDecoder
    given Encoder[NotFound] = deriveEncoder

    given Decoder[ServerError] = deriveDecoder
    given Encoder[ServerError] = deriveEncoder

    given Decoder[Unauthorized] = deriveDecoder
    given Encoder[Unauthorized] = deriveEncoder

    given Decoder[LastUpdatedTime] = deriveDecoder
    given Encoder[LastUpdatedTime] = deriveEncoder

    given Decoder[VideoReference] = deriveDecoder
    given Encoder[VideoReference] = deriveEncoder

    given Decoder[VideoReferenceCreate] = deriveDecoder
    given Encoder[VideoReferenceCreate] = deriveEncoder

    given Decoder[VideoReferenceUpdate] = deriveDecoder
    given Encoder[VideoReferenceUpdate] = deriveEncoder

    given Decoder[Video] = deriveDecoder
    given Encoder[Video] = deriveEncoder

    given Decoder[VideoUpdate] = deriveDecoder
    given Encoder[VideoUpdate] = deriveEncoder

    given Decoder[VideoSequence] = deriveDecoder
    given Encoder[VideoSequence] = deriveEncoder

    given Decoder[VideoSequenceCreate] = deriveDecoder
    given Encoder[VideoSequenceCreate] = deriveEncoder

    given Decoder[VideoSequenceUpdate] = deriveDecoder
    given Encoder[VideoSequenceUpdate] = deriveEncoder

    val CustomPrinter: Printer = Printer(
        dropNullValues = true,
        indent = ""
    )

    /**
     * Convert a circe Json object to a JSON string
     *
     * @param value
     *   Any value with an implicit circe coder in scope
     */
    extension (json: Json) def stringify: String = CustomPrinter.print(json)

    /**
     * Convert an object to a JSON string
     *
     * @param value
     *   Any value with an implicit circe coder in scope
     */
    extension [T: Encoder](value: T) 
        def stringify: String = Encoder[T]
            .apply(value)
            .deepDropNullValues
            .stringify

    extension [T: Decoder](jsonString: String) 
        def toJson: Either[ParsingFailure, Json] = parser.parse(jsonString);

    extension (jsonString: String)
        def reify[T: Decoder]: Either[Error, T] =
            for
                json   <- jsonString.toJson
                result <- Decoder[T].apply(json.hcursor)
            yield result
