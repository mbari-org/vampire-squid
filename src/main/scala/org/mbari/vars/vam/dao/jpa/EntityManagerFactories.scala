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

package org.mbari.vars.vam.dao.jpa

import java.util
import javax.persistence.{ EntityManagerFactory, Persistence }

import com.typesafe.config.ConfigFactory
import org.eclipse.persistence.config.PersistenceUnitProperties

import scala.collection.JavaConverters._

/**
 * https://stackoverflow.com/questions/4106078/dynamic-jpa-connection
 *
 * THis factory allows us to instantiate an javax.persistence.EntityManager from the
 * basic parameters (url, driver, password, username). You can pass in a map of additional properties
 * to customize the EntityManager.
 *
 * @author Brian Schlining
 * @since 2016-05-05T17:29:00
 */
object EntityManagerFactories {

  private lazy val config = ConfigFactory.load()

  val PRODUCTION_PROPS = Map(
    "eclipselink.connection-pool.default.initial" -> "2",
    "eclipselink.connection-pool.default.max" -> "16",
    "eclipselink.connection-pool.default.min" -> "2",
    "eclipselink.logging.session" -> "false",
    "eclipselink.logging.thread" -> "false",
    "eclipselink.logging.timestamp" -> "false",
    "javax.persistence.schema-generation.database.action" -> "create",
    PersistenceUnitProperties.SESSION_CUSTOMIZER -> "org.mbari.vars.vam.dao.jpa.UUIDSequence")

  def apply(properties: Map[String, String]): EntityManagerFactory = {
    val props = properties ++ PRODUCTION_PROPS
    Persistence.createEntityManagerFactory("video-asset-manager", props.asJava)
  }

  def apply(
    url: String,
    username: String,
    password: String,
    driverName: String,
    properties: Map[String, String] = Map.empty): EntityManagerFactory = {

    val map = Map(
      "javax.persistence.jdbc.url" -> url,
      "javax.persistence.jdbc.user" -> username,
      "javax.persistence.jdbc.password" -> password,
      "javax.persistence.jdbc.driver" -> driverName)
    apply(map ++ properties)
  }

  def apply(configNode: String): EntityManagerFactory = {
    val driver = config.getString(configNode + ".driver")
    val logLevel = config.getString("database.loglevel")
    val password = config.getString(configNode + ".password")
    val productName = config.getString(configNode + ".name")
    val url = config.getString(configNode + ".url")
    val user = config.getString(configNode + ".user")
    val props = Map(
      "eclipselink.logging.level" -> logLevel,
      "eclipselink.target-database" -> productName,
      "javax.persistence.database-product-name" -> productName,
      "javax.persistence.jdbc.driver" -> driver,
      "javax.persistence.jdbc.password" -> password,
      "javax.persistence.jdbc.url" -> url,
      "javax.persistence.jdbc.user" -> user)
    apply(props)
  }
}
