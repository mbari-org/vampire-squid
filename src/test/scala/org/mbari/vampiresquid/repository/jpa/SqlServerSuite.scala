package org.mbari.vampiresquid.repository.jpa

import org.testcontainers.utility.DockerImageName
import java.sql.DriverManager
import org.testcontainers.containers.PostgreSQLContainer
import scala.util.Random
import scala.jdk.CollectionConverters.*

class SqlServersSuite extends munit.FunSuite:

  val daoFactory = SqlServerDAOFactory

  override def beforeAll(): Unit = daoFactory.beforeAll()
  override def afterAll(): Unit  = daoFactory.afterAll()

  test("SqlServer container should be started"):
    assert(daoFactory.container.isRunning())
    val dao = daoFactory.newVideoDAO()
    val all = dao.findAll()
    assert(all.isEmpty)
    dao.close()

  test("SqlServer init script should have been run"):
    val em = daoFactory.entityManagerFactory.createEntityManager()
    val q  = em.createNativeQuery("SELECT COUNT(*) FROM unique_videos")
    val r = q.getResultList().asScala.toList.head.asInstanceOf[Long]
    println("---------------------" + r)
    assert(r == 0)
    
