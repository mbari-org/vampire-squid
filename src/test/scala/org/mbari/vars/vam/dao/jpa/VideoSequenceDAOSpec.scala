package org.mbari.vars.vam.dao.jpa

import org.scalatest.{FlatSpec, Matchers}

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-06T15:43:00
  */
class VideoSequenceDAOSpec extends FlatSpec with Matchers {

  "TestDAOFactory" should "create a VideoSequenceDAO" in {
    val dao = TestDAOFactory.newVideoSequenceDAO()
    dao should not be null
  }

}
