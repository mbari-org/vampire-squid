import javax.servlet.ServletContext

import org.mbari.vars.vam.api.{ VAMSwagger, VideoSequenceV1Api, VideoV1Api }
import org.mbari.vars.vam.controllers.VideoSequenceController
import org.mbari.vars.vam.dao.jpa.DerbyDAOFactory
import org.scalatra.LifeCycle
import org.scalatra.swagger.{ ApiInfo, Swagger }
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-20T14:41:00
 */
class ScalatraBootstrap extends LifeCycle {

  private[this] val log = LoggerFactory.getLogger(getClass)

  val apiInfo = ApiInfo(
    """video-asset-manager""",
    """Video Asset Manager - Server""",
    """http://localhost:8080/api-docs""",
    """brian@mbari.org""",
    """MIT""",
    """http://opensource.org/licenses/MIT""")

  implicit val swagger = new Swagger("1.2", "1.0.0", apiInfo)

  override def init(context: ServletContext): Unit = {

    println("STARTING UP NOW")

    implicit val executionContext = ExecutionContext.global

    val daoFactory = DerbyDAOFactory
    val videoSequenceController = new VideoSequenceController(daoFactory)
    val videoSequenceV1Api = new VideoSequenceV1Api(videoSequenceController)
    val videoV1Api = new VideoV1Api()

    context.mount(videoSequenceV1Api, "/v1/videosequence")
    context.mount(videoV1Api, "/v1/video")
    context.mount(new VAMSwagger, "/api-docs")

  }

}
