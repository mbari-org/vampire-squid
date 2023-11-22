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

import scala.reflect.ClassTag

@deprecated("Use ToStringTransforms instead", "0.0.1")
object Reflect:
    def toFormMap[T: ClassTag](t: T): Map[String, String] =
        val classTag = implicitly[ClassTag[T]]
        val fields   = classTag.runtimeClass.getDeclaredFields
        fields.flatMap { field =>
            field.setAccessible(true)
            field.get(t) match
                case Some(value) => Some(field.getName -> value.toString)
                case None        => None
                case value       => if value == null then None else Some(field.getName -> value.toString)
        }.toMap

    def fromFormMap[T: ClassTag](m: Map[String, ?]): T =
        val classTag        = implicitly[ClassTag[T]]
        val constructor     = classTag.runtimeClass.getDeclaredConstructors.head
        val constructorArgs = constructor
            .getParameters()
            .map { param =>
                val paramName = param.getName
                if param.getType == classOf[Option[?]] then m.get(paramName)
                else
                    m.getOrElse(
                        paramName,
                        throw new IllegalArgumentException(s"Missing required parameter: $paramName")
                    )
            }
        constructor.newInstance(constructorArgs*).asInstanceOf[T]
