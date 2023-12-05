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

package org.mbari.vampiresquid.repository

import org.mbari.vampiresquid.repository.jpa.entity.IPersistentObject

import java.net.URI
import java.util.UUID

/**
 * Defines the API methods used for accessing VideoReference objects
 *
 * @author
 *   Brian Schlining
 * @since 2016-05-05T14:51:00
 */
trait VideoReferenceDAO[T <: IPersistentObject] extends DAO[T]:

    def findAll(offset: Int, limit: Int): Iterable[T]
    def findAllURIs(): Iterable[URI]
    def findByVideoUUID(uuid: UUID): Iterable[T]
    def findConcurrent(uuid: UUID): Iterable[T]
    def findByURI(uri: URI): Option[T]
    def findByFileName(filename: String): Iterable[T]
    def findBySha512(sha: Array[Byte]): Option[T]
