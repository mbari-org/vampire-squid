package org.mbari.vars.vam.api

import org.mbari.vars.vam.Constants
import org.mbari.vars.vam.dao.jpa.H2TestDAOFactory
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
  protected[this] val daoFactory = H2TestDAOFactory
  protected[this] implicit val executionContext = ExecutionContext.global

  protected[this] val apiInfo = ApiInfo(
    """video-asset-manager""",
    """Video Asset Manager - Server""",
    """http://localhost:8080/api-docs""",
    """brian@mbari.org""",
    """MIT""",
    """http://opensource.org/licenses/MIT""")

  protected[this] implicit val swagger = new Swagger("1.2", "1.0.0", apiInfo)

}
