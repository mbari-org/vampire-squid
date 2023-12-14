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

package org.mbari.vampiresquid.repository.jpa.entity;

import java.util.UUID;
import java.time.Duration;
import java.time.Instant;
import java.net.URI;


/**
 * This is a DTO projection used by a named query in VideoSequenceEntity. It really shouldnt' be used outside of VideoEntity and
 * VideoSequenceDAOImpl (where it's an internal detail and not exposed via any API)
 * @param name
 * @param date
 */
public record Media(
  UUID videoSequenceUuid,
  UUID videoUuid,
  UUID videoReferenceUuid,
  String videoSequenceName,
  String cameraId,
  String videoName,
  URI uri,
  Instant startTimestamp,
  Duration duration,
  String container,
  String videoCodec,
  String audioCodec,
  Integer width,
  Integer height,
  Double frameRate,
  Long sizeBytes,
  String description,
  String videoSequenceDescription,
  String videoDescription,
  byte[] sha512
) {

}
