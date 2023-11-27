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

package org.mbari.vampiresquid

import sttp.tapir.*

import scala.concurrent.Future
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import org.mbari.vampiresquid.repository.jpa.JPADAOFactory
import org.mbari.vampiresquid.controllers.MediaController
import org.mbari.vampiresquid.etc.jwt.JwtService
import org.mbari.vampiresquid.endpoints.MediaEndpoints
import org.mbari.vampiresquid.endpoints.AuthorizationEndpoints
import org.mbari.vampiresquid.endpoints.HealthEndpoints
import scala.concurrent.ExecutionContext.Implicits.global
import org.mbari.vampiresquid.domain.VideoSequence
import org.mbari.vampiresquid.endpoints.VideoSequenceEndpoints
import org.mbari.vampiresquid.controllers.VideoSequenceController
import org.mbari.vampiresquid.controllers.VideoReferenceController
import org.mbari.vampiresquid.controllers.VideoController
import org.mbari.vampiresquid.endpoints.VideoEndpoints
import org.mbari.vampiresquid.endpoints.VideoReferenceEndpoints

object Endpoints:

    // ----------------------------
    val daoFactory               = JPADAOFactory
    val mediaController          = new MediaController(daoFactory)
    val videoSequenceController  = new VideoSequenceController(daoFactory)
    val videoController          = new VideoController(daoFactory)
    val videoReferenceController = new VideoReferenceController(daoFactory)

    // ----------------------------
    val jwtParams                = AppConfig.JwtParameters
    given jwtService: JwtService = new JwtService(jwtParams.issuer, jwtParams.clientSecret, jwtParams.signingSecret)

    // ----------------------------
    val mediaEndpoints          = new MediaEndpoints(mediaController, jwtService)
    val authEndpoints           = new AuthorizationEndpoints(jwtService)
    val healthEndpoints         = new HealthEndpoints
    val videoSequenceEndpoints  = new VideoSequenceEndpoints(videoSequenceController)
    val videoEndpoints          = new VideoEndpoints(videoController, videoSequenceController)
    val videoReferenceEndpoints = new VideoReferenceEndpoints(videoReferenceController)

    val apiEndpoints =
        mediaEndpoints.allImpl ++
            authEndpoints.allImpl ++
            healthEndpoints.allImpl ++
            videoSequenceEndpoints.allImpl ++
            videoEndpoints.allImpl ++
            videoReferenceEndpoints.allImpl

    val docEndpoints: List[ServerEndpoint[Any, Future]] = SwaggerInterpreter()
        .fromServerEndpoints[Future](apiEndpoints, "vampire-squid", "1.0.0")

    val prometheusMetrics: PrometheusMetrics[Future] = PrometheusMetrics.default[Future]()
    val metricsEndpoint: ServerEndpoint[Any, Future] = prometheusMetrics.metricsEndpoint

    val all: List[ServerEndpoint[Any, Future]] = apiEndpoints ++ docEndpoints ++ List(metricsEndpoint)
