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

package org.mbari.vampiresquid.etc.jdk

import java.time.Instant

class InstantsSuite extends munit.FunSuite:

    test("parseIso8601"):
        val now     = Instant.now();
        val formats = Seq(
            Instants.TimeFormatter,
            Instants.CompactTimeFormatter,
            Instants.CompactTimeFormatterMs,
            Instants.CompactTimeFormatterNs
        )
        for f <- formats
        do
            val s = f.format(now)
            Instants.parseIso8601(s) match
                case Right(value) => // do nothing
                case Left(e)      => fail(s"Failed to parse $s", e)
