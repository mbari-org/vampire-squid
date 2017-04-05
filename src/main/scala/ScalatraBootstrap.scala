import javax.servlet.ServletContext

import org.mbari.vars.vam.api._
import org.mbari.vars.vam.controllers.{ MediaController, VideoController, VideoReferenceController, VideoSequenceController }
import org.mbari.vars.vam.dao.jpa.JPADAOFactory
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

    val daoFactory = JPADAOFactory
    val mediaController = new MediaController(daoFactory)
    val videoSequenceController = new VideoSequenceController(daoFactory)
    val videoController = new VideoController(daoFactory)
    val videoReferenceController = new VideoReferenceController(daoFactory)

    val authorizationV1Api = new AuthorizationV1Api()
    val mediaV1Api = new MediaV1Api(mediaController)
    val mediaV2Api = new MediaV2Api(mediaController)
    val videoReferenceV1Api = new VideoReferenceV1Api(videoReferenceController)
    val videoReferenceV2Api = new VideoReferenceV2Api(videoReferenceController)
    val videoSequenceV1Api = new VideoSequenceV1Api(videoSequenceController)
    val videoV1Api = new VideoV1Api(videoController)
    val videoV2Api = new VideoV2Api(videoController)

    context.mount(authorizationV1Api, "/v1/auth")
    context.mount(mediaV1Api, "/v1/media")
    context.mount(mediaV2Api, "/v2/media")
    context.mount(videoSequenceV1Api, "/v1/videosequences")
    context.mount(videoV1Api, "/v1/videos")
    context.mount(videoV1Api, "/v2/videos")
    context.mount(videoReferenceV1Api, "/v1/videoreferences")
    context.mount(videoReferenceV1Api, "/v2/videoreferences")
    context.mount(new VAMSwagger, "/api-docs")

  }

}
