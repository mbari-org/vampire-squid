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

package org.mbari.vampiresquid.repository

/**
  * OUr intent is to eventually support different backends. This factory provides an abstraction
  * to obtaining the appropriate DAO.
  *
  * @author Brian Schlining
  * @since 2016-05-06T15:39:00
  */
trait DAOFactory[A <: PersistentObject, B <: PersistentObject, C <: PersistentObject] {

  def newVideoSequenceDAO(): VideoSequenceDAO[A]

  /**
    * Create a new DAO that share the underlying connection (e.g. EntityManager)
    * @param dao
    * @return
    */
  def newVideoSequenceDAO(dao: DAO[_]): VideoSequenceDAO[A]

  def newVideoDAO(): VideoDAO[B]

  /**
    * Create a new DAO that share the underlying connection (e.g. EntityManager)
    * @param dao
    * @return
    */
  def newVideoDAO(dao: DAO[_]): VideoDAO[B]

  def newVideoReferenceDAO(): VideoReferenceDAO[C]

  /**
    * Create a new DAO that share the underlying connection (e.g. EntityManager)
    * @param dao
    * @return
    */
  def newVideoReferenceDAO(dao: DAO[_]): VideoReferenceDAO[C]

}
