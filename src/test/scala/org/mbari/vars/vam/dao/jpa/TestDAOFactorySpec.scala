package org.mbari.vars.vam.dao.jpa

import org.scalatest.{ FlatSpec, Matchers }

/**
 * Created by brian on 5/12/16.
 */
class TestDAOFactorySpec extends FlatSpec with Matchers {

  "TestDAOFactory" should "create a VideoSequenceDAO" in {
    val dao = TestDAOFactory.newVideoSequenceDAO()
    dao should not be null
    dao.close()
  }

}
