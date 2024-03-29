/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
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

package org.mbari.vampiresquid.repository.jpa

import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager
import org.testcontainers.containers.PostgreSQLContainer
import scala.util.Random
import scala.jdk.CollectionConverters.*

class PostgresSuite extends munit.FunSuite:

    val daoFactory = PostgresqlTestDAOFactory

    override def beforeAll(): Unit = daoFactory.beforeAll()
    override def afterAll(): Unit  = daoFactory.afterAll()

    test("Postgres container should be started"):
        assert(daoFactory.container.isRunning())
        val dao = daoFactory.newVideoDAO()
        val all = dao.findAll(0, 100)
        assert(all.isEmpty)
        dao.close()

    test("Postgres init script should have been run"):
        val em = daoFactory.entityManagerFactory.createEntityManager()
        val q  = em.createNativeQuery("SELECT COUNT(*) FROM unique_videos")
        val r  = q.getResultList().asScala.toList.head.asInstanceOf[Long]
        println("---------------------" + r)
        assert(r == 0)
