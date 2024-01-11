/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
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

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import sttp.tapir.server.vertx.{VertxFutureServerInterpreter, VertxFutureServerOptions}
import sttp.tapir.server.vertx.VertxFutureServerInterpreter.VertxFutureToScalaFuture

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import ExecutionContext.Implicits.global
import scala.io.StdIn
import sttp.tapir.server.interceptor.log.ServerLog
import org.mbari.vampiresquid.etc.jdk.Logging
import org.mbari.vampiresquid.etc.jdk.Logging.{*, given}
import sttp.tapir.server.interceptor.log.DefaultServerLog

@main
def run(): Unit =

    val log = Logging("Main")

    val serverOptions = VertxFutureServerOptions
        .customiseInterceptors
        .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())
        .options

    val port = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(8080)

    val vertx  = Vertx.vertx()
    val server = vertx.createHttpServer()
    val router = Router.router(vertx)

    // Log all requests
    router.route().handler(ctx => log.atInfo.log(s"${ctx.request().method()} ${ctx.request().path()}"))

    Endpoints
        .all
        .foreach(endpoint =>
            VertxFutureServerInterpreter(serverOptions)
                .route(endpoint)
                .apply(router)
        )

    val program = for
        binding <- server.requestHandler(router).listen(port).asScala
        _       <- Future:
                       println(
                           s"Go to http://localhost:${binding.actualPort()}/docs to open SwaggerUI. Press ENTER key to exit."
                       )
                       StdIn.readLine()
        stop    <- binding.close().asScala
    yield stop

    Await.result(program, Duration.Inf)
