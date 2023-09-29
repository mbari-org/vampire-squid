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

import ToStringTransforms.{given}
import FormTransform.given
import org.mbari.vampiresquid.repository.jpa.TestUtils
import org.mbari.vampiresquid.domain.Media

class FormTransformSuite extends munit.FunSuite:
  
  test("transform(media: Media)"):
    val vs = TestUtils.build(1, 1, 1).head
    val media = Media.from(vs.getVideoReferences().get(0))
    val s = ToStringTransforms.transform(media)
    println(s)
