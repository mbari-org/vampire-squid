package org.mbari.vars.vam.controllers

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.dao.jpa.JPADAOFactory

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-23T13:51:00
 */
trait BaseController {

  private[this] val gson = Constants.GSON

  def daoFactory: JPADAOFactory
  def toJson(obj: Any): String = gson.toJson(obj)
  def fromJson[T](json: String, classOfT: Class[T]): T = gson.fromJson(json, classOfT)

}
