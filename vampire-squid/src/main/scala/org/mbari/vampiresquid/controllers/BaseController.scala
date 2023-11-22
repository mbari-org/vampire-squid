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

package org.mbari.vampiresquid.controllers

import io.circe.{Decoder, Encoder}
import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.etc.circe.CirceCodecs.{*, given}
import io.circe.parser.decode

/**
 * @author
 *   Brian Schlining
 * @since 2016-05-23T13:51:00
 */
trait BaseController:

    def daoFactory: JPADAOFactory
    def toJson[T: Encoder](obj: T): String    = obj.stringify
    def fromJson[T: Decoder](json: String): T = decode[T](json) match
        case Left(e)      => throw new RuntimeException(e)
        case Right(value) => value
