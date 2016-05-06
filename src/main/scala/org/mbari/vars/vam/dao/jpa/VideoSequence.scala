package org.mbari.vars.vam.dao.jpa


import javax.persistence._
import java.util.{ArrayList => JArrayList, List => JList}
import scala.collection.JavaConverters._


/**
  *
  *
  * @author Brian Schlining
  * @since 2016-05-05T12:09:00
  */
class VideoSequence extends HasUUID with HasOptimisticLock {


  @Column(name = "name",
    nullable = false,
    length = 512,
    unique = true)
  var name: String = _

  @Column(name = "camera_id",
    nullable = false,
    length = 256)
  var cameraID: String = _

  @OneToMany(
    targetEntity = classOf[Video],
    cascade = Array(CascadeType.ALL),
    fetch = FetchType.EAGER,
    mappedBy = "videoSequence")
  private var javaVideos: JList[Video] = new JArrayList[Video]

  def addVideo(video: Video): Unit = javaVideos.add(video)
  def removeVideo(video: Video): Unit = javaVideos.remove(video)
  def videos: Seq[Video]  = javaVideos.asScala

}
