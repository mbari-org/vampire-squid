package org.mbari.vars.vam.api

/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-23T18:38:00
  */
case class FailedRequest(message: String, exception: Throwable = null)
