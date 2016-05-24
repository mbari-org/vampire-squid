package org.mbari.vars.vam.dao

import javax.persistence.EntityManager

import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-06T13:30:00
 */
package object jpa {

  type JPADAOFactory = DAOFactory[VideoSequence, Video, VideoReference]

}
