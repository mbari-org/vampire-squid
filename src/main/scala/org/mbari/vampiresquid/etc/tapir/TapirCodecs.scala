/*
 * Copyright 2021 MBARI
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

package org.mbari.vampiresquid.etc.tapir

import sttp.tapir.{Codec, DecodeResult}
import sttp.tapir.CodecFormat.TextPlain

import java.net.URI
import scala.util.{Failure, Success, Try}

object TapirCodecs:

  private def decodeUri(s: String): DecodeResult[URI] =
    Try(URI.create(s)) match
      case Success(uri) => DecodeResult.Value(uri)
      case Failure(e)   => DecodeResult.Error(s"Failed to decode s to a URI", e)
  private def encodeUri(u: URI): String               = u.toString
  given uriCodec: Codec[String, URI, TextPlain]       = Codec.string.mapDecode(decodeUri)(encodeUri)
