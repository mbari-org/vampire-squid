package org.mbari.vars.vam.dao.jpa

import javax.persistence.Column

import com.google.gson.annotations.Expose

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-06-02T14:53:00
 */
trait HasDescription {

  @Expose(serialize = true)
  @Column(
    name = "description",
    length = 2048)
  var description: String = _

}
