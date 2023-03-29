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

package org.mbari.vampiresquid.repository.jpa.entity;

import org.mbari.vampiresquid.etc.jpa.TransactionLogger;
import org.mbari.vampiresquid.etc.jpa.UUIDConverter;
import org.mbari.vampiresquid.repository.PersistentObject;
import scala.Option;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "VideoSequence")
@Table(
        name = "video_sequences",
        indexes = {
                @Index(name = "idx_video_sequences__name", columnList = "name"),
                @Index(name = "idx_video_sequences__camera_id", columnList = "camera_id")
        }
)
@EntityListeners({TransactionLogger.class})
@NamedNativeQueries(
        {
                @NamedNativeQuery(
                        name = "VideoSequence.findAllNames",
                        query = "SELECT name FROM video_sequences ORDER BY name ASC"
                ),
                @NamedNativeQuery(
                        name = "VideoSequence.findNamesByCameraID",
                        query = "SELECT name FROM video_sequences WHERE camera_id = ?1 ORDER BY name ASC"
                ),
                @NamedNativeQuery(
                        name = "VideoSequence.findAllCameraIDs",
                        query = "SELECT DISTINCT camera_id FROM video_sequences ORDER BY camera_id ASC"
                )
        }
)
@NamedQueries(
        {
                @NamedQuery(name = "VideoSequence.findAll", query = "SELECT v FROM VideoSequence v"),
                @NamedQuery(
                        name = "VideoSequence.findByCameraID",
                        query = "SELECT v FROM VideoSequence v WHERE v.cameraID = :cameraID"
                ),
                @NamedQuery(
                        name = "VideoSequence.findByName",
                        query = "SELECT v FROM VideoSequence v WHERE v.name = :name"
                ),
                @NamedQuery(
                        name = "VideoSequence.findByVideoUUID",
                        query = "SELECT v FROM VideoSequence v LEFT JOIN v.videos w WHERE w.uuid = :uuid"
                ),
                @NamedQuery(
                        name = "VideoSequence.findBetweenDates",
                        query =
                                "SELECT v FROM VideoSequence v LEFT JOIN v.videos w WHERE w.start BETWEEN :startDate AND :endDate"
                ),
                @NamedQuery(
                        name = "VideoSequence.findByNameAndBetweenDates",
                        query =
                                "SELECT v FROM VideoSequence v LEFT JOIN v.videos w WHERE v.name = :name AND w.start BETWEEN :startDate AND :endDate"
                ),
                @NamedQuery(
                        name = "VideoSequence.findByCameraIDAndBetweenDates",
                        query =
                                "SELECT v FROM VideoSequence v LEFT JOIN v.videos w WHERE v.cameraID = :cameraID AND w.start BETWEEN :startDate AND :endDate"
                )
        }
)
public class VideoSequenceEntity implements PersistentObject {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @Column(name = "uuid", nullable = false, updatable = false, length = 36)
    @Convert(converter = UUIDConverter.class)
    UUID uuid;

//    def primaryKey: Option[UUID] = Option(uuid)

//    def lastUpdated: Option[Instant] = Option(lastUpdatedTime).map(_.toInstant)

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 512, unique = true)
    String name;

//    @Expose(serialize = true)
//    @SerializedName(value = "camera_id")
    @Basic(optional = false)
    @Column(name = "camera_id", nullable = false, length = 256)
    String cameraID;

//    @Expose(serialize = true)
//    @SerializedName(value = "videos")
    @OneToMany(
            targetEntity = VideoEntity.class,
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER,
            mappedBy = "videoSequence",
            orphanRemoval = true
    )
    List<VideoEntity> videos = new ArrayList<>();

    @Column(name = "description", length = 2048)
    String description;

    /** Optimistic lock to prevent concurrent overwrites */
    @Version
    @Column(name = "last_updated_time")
    protected Timestamp lastUpdatedTime;

    public VideoSequenceEntity() {

    }

    public VideoSequenceEntity(String name, String cameraID, String description) {
        this.name = name;
        this.cameraID = cameraID;
        this.description = description;
    }

    @Override
    public Option<UUID> primaryKey() {
        return null;
    }

    public List<VideoEntity> getVideos() {
        return videos;
    }

    public void addVideo(VideoEntity video) {
        videos.add(video);
        video.setVideoSequence(this);
    }

    public void removeVideo(VideoEntity video) {
        videos.remove(video);
        video.setVideoSequence(null);
    }
//    def videos: Seq[Video] = javaVideos.asScala.toSeq

    public List<VideoReferenceEntity> getVideoReferences() {
        return videos.stream()
                .flatMap(v -> v.getVideoReferences().stream())
                .toList();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCameraID() {
        return cameraID;
    }

    public void setCameraID(String cameraID) {
        this.cameraID = cameraID;
    }

    public Timestamp getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public boolean canEqual(Object other) {
        return other instanceof VideoReferenceEntity;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VideoSequenceEntity that) {
            return that.canEqual(this) && name.equals(that.name);
        }
        return false;

    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31;
    }

    @Override
    public String toString() {
        return "VideoSequence(%s, %s)".formatted(name, cameraID);
    }

}
