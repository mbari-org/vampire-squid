/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-20T14:33:00
 */
class JettyMain {

}
import com.typesafe.config.ConfigFactory
import org.eclipse.jetty.server._
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyMain {

  object conf {
    private[this] val config = ConfigFactory.load()
    val port = config.getInt("http.port")
    val stopTimeout = config.getInt("http.stop.timeout")
    val connectorIdleTimeout = config.getInt("http.connector.idle.timeout")
    val webapp = config.getString("http.webapp")
    val contextPath = config.getString("http.context.path")
  }

  def main(args: Array[String]) = {
    System.setProperty("user.timezone", "UTC")
    val server: Server = new Server
    println("starting jetty")

    server setStopTimeout conf.stopTimeout
    //server setDumpAfterStart true
    server setStopAtShutdown true

    val httpConfig = new HttpConfiguration()
    httpConfig setSendDateHeader true
    httpConfig setSendServerVersion false

    val connector = new NetworkTrafficServerConnector(server, new HttpConnectionFactory(httpConfig))
    connector setPort conf.port
    connector setSoLingerTime 0
    connector setIdleTimeout conf.connectorIdleTimeout
    server addConnector connector

    val webapp = conf.webapp
    val webApp = new WebAppContext
    webApp setContextPath conf.contextPath
    webApp setResourceBase conf.webapp
    webApp setEventListeners Array(new ScalatraListener)

    server setHandler webApp

    server.start()
  }
}
