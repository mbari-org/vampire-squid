package org.mbari.vars.vam.dao.jpa

/**
 * Marker Exception for when you do a query (such as by primary key) and a match is not found.
 *
 * @author Brian Schlining
 * @since 2016-05-26T10:11:00
 */
class NotFoundInDatastoreException(msg: String) extends RuntimeException(msg)
