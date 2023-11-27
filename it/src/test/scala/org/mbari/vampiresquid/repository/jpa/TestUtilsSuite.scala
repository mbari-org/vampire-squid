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

package org.mbari.vampiresquid.repository.jpa

import scala.util.Random
import scala.concurrent.ExecutionContext

class TestUtilsSuite extends munit.FunSuite:

    given JPADAOFactory    = TestDAOFactory.Instance
    given ExecutionContext = ExecutionContext.global

    test("randomVideoReference"):
        val videoReference = TestUtils.randomVideoReference()
        assert(videoReference.getUri != null)
        assert(videoReference.getUuid == null) // UUID not assigned until persisted

    test("randomSha512"):
        val sha = TestUtils.randomSha512();
        assertEquals(sha.length, 64)

    test("create"):
        for i <- 1 to 4
        do
            val a  = TestUtils.create(i, i, i)
            assertEquals(a.size, i)
            val vs = a.head
            val v  = vs.getVideos
            assertEquals(v.size(), i)
            val vr = v.get(0).getVideoReferences
            assertEquals(vr.size(), i)
