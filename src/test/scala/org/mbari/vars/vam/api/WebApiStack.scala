/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
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

package org.mbari.vars.vam.api

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.dao.jpa.{ DevelopmentTestDAOFactory, H2TestDAOFactory }
import org.scalatest.BeforeAndAfterAll
import org.scalatra.swagger.{ ApiInfo, Swagger }
import org.scalatra.test.scalatest.ScalatraFlatSpec

import scala.concurrent.ExecutionContext

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-08-11T17:09:00
 */
trait WebApiStack extends ScalatraFlatSpec with BeforeAndAfterAll {

  protected[this] val gson = Constants.GSON
  protected[this] val daoFactory = DevelopmentTestDAOFactory
  protected[this] implicit val executionContext = ExecutionContext.global

  protected[this] val apiInfo = ApiInfo(
    """video-asset-manager""",
    """Video Asset Manager - Server""",
    """http://localhost:8080/api-docs""",
    """brian@mbari.org""",
    """MIT""",
    """http://opensource.org/licenses/MIT""")

  protected[this] implicit val swagger = new Swagger("1.2", "1.0.0", apiInfo)

  protected override def afterAll(): Unit = daoFactory.cleanup()
}
