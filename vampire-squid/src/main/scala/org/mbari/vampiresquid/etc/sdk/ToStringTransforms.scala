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

package org.mbari.vampiresquid.etc.sdk

import java.net.URL
import java.nio.file.Path
import java.time.Instant
import java.util.UUID
import java.net.URI
import java.util.HexFormat

/**
 * This object contains type classes used to convert types to Strings.
 */
object ToStringTransforms:

    private val hex = HexFormat.of()

    /**
     * Base function we use to convert case classes to CSV
     * @param a
     *   The object to convert
     */
    def transform[A: Transformer](a: A) = summon[Transformer[A]].f(a)

    // Base trait
    trait Transformer[T]:
        def f(t: T): String

    // Create a type class of T => String for every type in your case class
    given Transformer[String] with
        def f(x: String) = x

    given Transformer[Boolean] with
        def f(x: Boolean) = x.toString

    given Transformer[Int] with
        def f(x: Int) = x.toString

    given Transformer[Long] with
        def f(x: Long) = x.toString

    given Transformer[Double] with
        def f(x: Double) = f"$x%.6f"

    given Transformer[URL] with
        def f(x: URL) = x.toExternalForm

    given Transformer[URI] with
        def f(x: URI) = x.toString

    given Transformer[Path] with
        def f(x: Path) = x.getFileName.toString

    given Transformer[Instant] with
        def f(x: Instant) = x.toString

    given Transformer[Array[Byte]] with
        def f(x: Array[Byte]) = hex.formatHex(x)

    given Transformer[UUID] with
        def f(x: UUID) = x.toString

    given Transformer[Any] with
        def f(x: Any) = x.toString()

    given [T](using t: Transformer[T]): Transformer[Option[T]] =
        new Transformer[Option[T]]:
            def f(x: Option[T]) = x match
                case None    => ""
                case Some(x) => t.f(x)
