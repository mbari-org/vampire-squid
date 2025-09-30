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

package org.mbari.vampiresquid.etc.sdk

import org.mbari.vampiresquid.etc.sdk.ToStringTransforms.Transformer

import scala.compiletime.summonAll
import scala.deriving.*

/**
 * Contains a ToStringTransform for converting a case class to HTTP form data.
 */
object FormTransform:

    inline given [A <: Product](using m: Mirror.ProductOf[A]): Transformer[A] =
        new Transformer[A]:
            type ElemTransformers = Tuple.Map[m.MirroredElemTypes, Transformer]
            val elemTransformers = summonAll[ElemTransformers].toList.asInstanceOf[List[Transformer[Any]]]
            def f(a: A): String  =
                val names       = a.productElementNames.toList
                val elems       = a.productIterator.toList
                val transformed = elems.zip(elemTransformers) map { (elem, transformer) =>
                    transformer.f(elem)
                }
                names
                    .zip(transformed)
                    .filter((name, value) => value != null && !value.isBlank)
                    .map((name, value) => s"$name=$value")
                    .mkString("&")
