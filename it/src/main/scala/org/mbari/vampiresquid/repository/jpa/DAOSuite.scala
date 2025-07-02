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

package org.mbari.vampiresquid.repository.jpa

import org.mbari.vampiresquid.repository.{DAO, VideoDAO}

import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration as SDuration
import scala.concurrent.ExecutionContext.Implicits.global

trait BaseDAOSuite extends munit.FunSuite:

    def daoFactory: SpecDAOFactory
    private val timeout = SDuration(2, TimeUnit.SECONDS)

    def exec[T](future: Future[T]): T = Await.result(future, timeout)

    def run[T](thunk: () => T)(using dao: DAO[?]): T =
        exec(dao.runTransaction(_ => thunk.apply()))

    override def afterEach(context: AfterEach): Unit =
        super.afterEach(context)
        daoFactory.cleanup()

// trait DAOSuite extends BaseDAOSuite:

//     given daoFactory: SpecDAOFactory = TestDAOFactory.Instance
