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

import org.slf4j.LoggerFactory
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

class JettyMain {}
import com.typesafe.config.ConfigFactory
import javax.servlet.DispatcherType
import net.bull.javamelody.{MonitoringFilter, Parameter, ReportServlet, SessionListener}
import org.eclipse.jetty.server._
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyMain {

  object conf {
    private[this] val config = ConfigFactory.load()
    val port                 = config.getInt("http.port")
    val stopTimeout          = config.getInt("http.stop.timeout")
    val connectorIdleTimeout = config.getInt("http.connector.idle.timeout")
    val webapp               = config.getString("http.webapp")
    val contextPath          = config.getString("http.context.path")
  }

  def main(args: Array[String]) = {
    System.setProperty("user.timezone", "UTC")
    val s = """
      | ____   ____                     .__                   _________            .__    .___
      | \   \ /   /____    _____ ______ |__|______   ____    /   _____/ ________ __|__| __| _/
      |  \   Y   /\__  \  /     \\____ \|  \_  __ \_/ __ \   \_____  \ / ____/  |  \  |/ __ | 
      |   \     /  / __ \|  Y Y  \  |_> >  ||  | \/\  ___/   /        < <_|  |  |  /  / /_/ | 
      |    \___/  (____  /__|_|  /   __/|__||__|    \___  > /_______  /\__   |____/|__\____ | 
      |                \/      \/|__|                   \/          \/    |__|             \/ """.stripMargin
    println(s)


    val server: Server = new Server
    LoggerFactory.getLogger(getClass)
      .atInfo
      .log("Starting Jetty server on port {}", conf.port)

    server setStopTimeout conf.stopTimeout.toLong
    //server setDumpAfterStart true
    server setStopAtShutdown true

    val httpConfig = new HttpConfiguration()
    httpConfig setSendDateHeader true
    httpConfig setSendServerVersion false

    val connector = new NetworkTrafficServerConnector(server, new HttpConnectionFactory(httpConfig))
    connector setPort conf.port
    connector setIdleTimeout conf.connectorIdleTimeout.toLong
    server addConnector connector

    val webApp = new WebAppContext
    webApp setContextPath conf.contextPath
    webApp setResourceBase conf.webapp
    webApp setEventListeners Array(new ScalatraListener)

    // Add JavaMelody for monitoring
    webApp.addServlet(classOf[ReportServlet], "/monitoring")
    webApp.addEventListener(new SessionListener)
    val monitoringFilter = new FilterHolder(new MonitoringFilter())
    monitoringFilter.setInitParameter(Parameter.APPLICATION_NAME.getCode, conf.webapp)
    monitoringFilter.setInitParameter("authorized-users", "adminz:Cranchiidae")
    webApp.addFilter(
      monitoringFilter,
      "/*",
      java.util.EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC)
    )

    server setHandler webApp

    server.start()
  }
}
