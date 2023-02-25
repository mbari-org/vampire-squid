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

import org.mbari.vampiresquid.api.{AuthorizationV1Api, HealthApi, MediaV1Api, VideoReferenceV1Api, VideoSequenceV1Api, VideoV1Api}
import org.mbari.vampiresquid.controllers.{MediaController, VideoController, VideoReferenceController, VideoSequenceController}
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory

import javax.servlet.ServletContext
import org.mbari.vampiresquid.api._
import org.scalatra.LifeCycle
// import org.scalatra.swagger.{ApiInfo, Swagger}

import org.mbari.vampiresquid.AppConfig
import scala.concurrent.ExecutionContext
import org.slf4j.LoggerFactory
// import org.scalatra.swagger.ContactInfo
// import org.scalatra.swagger.LicenseInfo

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-20T14:41:00
  */
class ScalatraBootstrap extends LifeCycle {

  // val apiInfo = ApiInfo(
  //   "vampire-squid",
  //   "A Video Asset Managment microservice0",
  //   "http://www.mbari.org",
  //   ContactInfo("Brian Schlining", "http://www.mbari.org", "brian@mbari.org"),
  //   LicenseInfo("Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0")
  // )

  // implicit val swagger = new Swagger("1.2", "1.0.0", apiInfo)

  override def init(context: ServletContext): Unit = {

    LoggerFactory.getLogger(getClass).info(s"Mounting ${AppConfig.Name} Servlets")
    // Optional because * is the default
    context.setInitParameter("org.scalatra.cors.allowedOrigins", "*")
    // Disables cookies, but required because browsers will not allow passing credentials to wildcard domains
    context.setInitParameter("org.scalatra.cors.allowCredentials", "false")

    implicit val executionContext = ExecutionContext.global

    val daoFactory               = JPADAOFactory
    val mediaController          = new MediaController(daoFactory)
    val videoSequenceController  = new VideoSequenceController(daoFactory)
    val videoController          = new VideoController(daoFactory)
    val videoReferenceController = new VideoReferenceController(daoFactory)

    val authorizationV1Api = new AuthorizationV1Api()
    val mediaV1Api         = new MediaV1Api(mediaController)
    //val mediaV2Api = new MediaV2Api(mediaController)
    val videoReferenceV1Api = new VideoReferenceV1Api(videoReferenceController)
    //val videoReferenceV2Api = new VideoReferenceV2Api(videoReferenceController)
    val videoSequenceV1Api = new VideoSequenceV1Api(videoSequenceController)
    //val videoSequenceV2Api = new VideoSequenceV2Api(videoSequenceController)
    val videoV1Api = new VideoV1Api(videoController)
    //val videoV2Api = new VideoV2Api(videoController)

    context.mount(authorizationV1Api, "/v1/auth")
    //context.mount(authorizationV1Api, "/v2/auth")
    context.mount(mediaV1Api, "/v1/media")
    //context.mount(mediaV2Api, "/v2/media")
    context.mount(videoSequenceV1Api, "/v1/videosequences")
    //context.mount(videoSequenceV2Api, "/v2/videosequences")
    context.mount(videoV1Api, "/v1/videos")
    //context.mount(videoV2Api, "/v2/videos")
    context.mount(videoReferenceV1Api, "/v1/videoreferences")
    //context.mount(videoReferenceV2Api, "/v2/videoreferences")
    context.mount(new HealthApi, "/v1/health")

  }

}
