package org.mbari.vars.vam.api

import org.mbari.vars.vam.controllers.VideoController

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-08-11T17:05:00
 */
class VideoApiSpec extends WebApiStack {

  private[this] val videoV1Api = {
    val videoController = new VideoController(daoFactory)
    new VideoV1Api(videoController)
  }

  addServlet(videoV1Api, "/v1/video")

  "VideoV1Api" should "return an empty JSON array when the database is empty" in {
    get("/v1/video") {
      status should be (200)
      body should equal("[]")
    }
  }

  protected override def afterAll(): Unit = {
    val dao = daoFactory.newVideoDAO()

    dao.runTransaction(d => {
      val all = dao.findAll()
      all.foreach(dao.delete)
    })
    dao.close()

    super.afterAll()
  }

}
