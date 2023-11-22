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

package org.mbari.vampiresquid.etc.jdk

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import scala.util.Try

object Instants:

    private val utcZone                           = ZoneId.of("UTC")
    val TimeFormatter: DateTimeFormatter          = DateTimeFormatter.ISO_DATE_TIME.withZone(utcZone)
    val CompactTimeFormatter: DateTimeFormatter   = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX").withZone(utcZone)
    val CompactTimeFormatterMs: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSX").withZone(utcZone)
    val CompactTimeFormatterNs: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSSSSSX").withZone(utcZone)

    def parseIso8601(s: String): Either[Throwable, Instant] =
        val tried = Try(Instant.from(CompactTimeFormatter.parse(s))) orElse
            Try(Instant.from(TimeFormatter.parse(s))) orElse
            Try(Instant.from(CompactTimeFormatterMs.parse(s))) orElse
            Try(Instant.from(CompactTimeFormatterNs.parse(s)))
        tried.toEither
