package org.mbari.vars.vam.dao.jpa

import javax.persistence.EntityManagerFactory

import com.typesafe.config.ConfigFactory


/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-06T11:04:00
  */
object TestDB {

  private[this] val config = ConfigFactory.load()

  lazy val entityManagerFactory: EntityManagerFactory = {
    val driver = config.getString("org.mbari.vars.vam.database.development.driver")
    val url = config.getString("org.mbari.vars.vam.database.development.url")
    val user = config.getString("org.mbari.vars.vam.database.development.user")
    val password = config.getString("org.mbari.vars.vam.database.development.password")
    EntityManagerFactories(url, user, password, driver)
  }

}
