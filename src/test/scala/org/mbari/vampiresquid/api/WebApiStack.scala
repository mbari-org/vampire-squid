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

package org.mbari.vampiresquid.api

import org.mbari.vampiresquid.Constants
import org.mbari.vampiresquid.repository.jpa.{DevelopmentTestDAOFactory, H2TestDAOFactory}
import org.scalatest.BeforeAndAfterAll
import org.scalatra.swagger.{ApiInfo, Swagger}
import org.scalatra.test.scalatest.ScalatraFlatSpec

import scala.concurrent.ExecutionContext
import org.scalatra.swagger.ContactInfo
import org.scalatra.swagger.LicenseInfo
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-08-11T17:09:00
  */
trait WebApiStack extends ScalatraFlatSpec with BeforeAndAfterAll {

  protected[this] val gson                      = Constants.GSON
  protected[this] val daoFactory                = DevelopmentTestDAOFactory
  implicit protected[this] val executionContext = ExecutionContext.global

  def exec[A](f: Future[A]): A = Await.result(f, 10.seconds)

  val apiInfo = ApiInfo(
    "vampire-squid",
    "A Video Asset Managment microservice0",
    "http://www.mbari.org",
    ContactInfo("Brian Schlining", "http://www.mbari.org", "brian@mbari.org"),
    LicenseInfo("Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0")
  )

  implicit protected[this] val swagger = new Swagger("1.2", "1.0.0", apiInfo)

  override protected def afterAll(): Unit = daoFactory.cleanup()
}
