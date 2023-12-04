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

import jakarta.persistence.{EntityManagerFactory, Persistence}

import com.typesafe.config.ConfigFactory

import scala.jdk.CollectionConverters.*

/**
 * https://stackoverflow.com/questions/4106078/dynamic-jpa-connection
 *
 * THis factory allows us to instantiate an javax.persistence.EntityManager from the basic parameters (url, driver,
 * password, username). You can pass in a map of additional properties to customize the EntityManager.
 *
 * @author
 *   Brian Schlining
 * @since 2016-05-05T17:29:00
 */
object EntityManagerFactories:

    private lazy val config = ConfigFactory.load()

    // https://juliuskrah.com/tutorial/2017/02/16/getting-started-with-hikaricp-hibernate-and-jpa/
    val PRODUCTION_PROPS = Map(
        "hibernate.connection.provider_class" -> "org.hibernate.hikaricp.internal.HikariCPConnectionProvider",
        "hibernate.hbm2ddl.auto"              -> "validate",
        "hibernate.hikari.idleTimeout"        -> "30000",
        "hibernate.hikari.maximumPoolSize"    -> "16",
        "hibernate.hikari.minimumIdle"        -> "2"
    )

    def apply(properties: Map[String, String]): EntityManagerFactory =
        val props = PRODUCTION_PROPS ++ properties
        Persistence.createEntityManagerFactory("video-asset-manager", props.asJava)

    def apply(
        url: String,
        username: String,
        password: String,
        driverName: String,
        properties: Map[String, String] = Map.empty
    ): EntityManagerFactory =

        val map = Map(
            "jakarta.persistence.jdbc.url"      -> url,
            "jakarta.persistence.jdbc.user"     -> username,
            "jakarta.persistence.jdbc.password" -> password,
            "jakarta.persistence.jdbc.driver"   -> driverName
        )
        apply(map ++ properties)

    def apply(configNode: String): EntityManagerFactory =
        val driver      = config.getString(configNode + ".driver")
        val logLevel    = config.getString(configNode + ".loglevel")
        val password    = config.getString(configNode + ".password")
        val productName = config.getString(configNode + ".name")
        val url         = config.getString(configNode + ".url")
        val user        = config.getString(configNode + ".user")
        val props       = Map(
            "hibernate.dialect"                 -> productName,
            "jakarta.persistence.jdbc.driver"   -> driver,
            "jakarta.persistence.jdbc.password" -> password,
            "jakarta.persistence.jdbc.url"      -> url,
            "jakarta.persistence.jdbc.user"     -> user
        )
        apply(props)
