import akka.actor.ActorSystem

import org.scalatra.swagger._
import org.scalatra.ScalatraServlet
import org.json4s.{ DefaultFormats, Formats }

class ResourcesApp(implicit protected val system: ActorSystem, val swagger: SwaggerApp)
    extends ScalatraServlet with NativeSwaggerBase {

  before() {
    response.headers += ("Access-Control-Allow-Origin" -> "*")
  }

  protected def buildFullUrl(path: String) = if (path.startsWith("http")) path else {
    val port = request.getServerPort
    val h = request.getServerName
    val prot = if (port == 443) "https" else "http"
    val (proto, host) = if (port != 80 && port != 443) ("http", h + ":" + port.toString) else (prot, h)
    "%s://%s%s%s".format(
      proto,
      host,
      request.getContextPath,
      path
    )
  }
}

class SwaggerApp extends Swagger(apiInfo = ApiSwagger.apiInfo, apiVersion = "1.0", swaggerVersion = "1.2")

object ApiSwagger {
  val apiInfo = ApiInfo(
    """video-asset-manager""",
    """Video Asset Manager - Server""",
    """http://localhost:8080/api-docs""",
    """brian@mbari.org""",
    """MIT""",
    """http://opensource.org/licenses/MIT"""
  )
}