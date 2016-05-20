import javax.servlet.ServletContext

import org.scalatra.LifeCycle
import org.slf4j.LoggerFactory

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-20T14:41:00
  */
class ScalatraBootstrap extends LifeCycle {

  private[this] val log = LoggerFactory.getLogger(getClass)

  implicit val swagger = new SwaggerApp

  override def init(context: ServletContext): Unit = {

  }

}
