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
import org.mbari.vampiresquid.etc.jpa.InstantConverter;
import org.mbari.vampiresquid.repository.PersistentObject;
import scala.Option;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "Video")
@Table(
        name = "videos",
        indexes = {
                @Index(name = "idx_videos__name", columnList = "name"),
                @Index(name = "idx_videos__start_time", columnList = "start_time"),
                @Index(name = "idx_videos__video_sequence_uuid", columnList = "video_sequence_uuid")
        }
)
@EntityListeners({TransactionLogger.class})
@NamedNativeQueries(
        {
                @NamedNativeQuery(
                        name = "Video.findAllNames",
                        query = "SELECT name FROM videos ORDER BY name"
                ),
                @NamedNativeQuery(
                        name = "Video.findNamesByVideoSequenceName",
                        query =
                                "SELECT v.name FROM videos v LEFT JOIN video_sequences vs ON v.video_sequence_uuid = vs.uuid WHERE vs.name = ?1 ORDER BY v.name ASC"
                ),
                @NamedNativeQuery(
                        name = "Video.findAllNamesAndStartDates",
                        query = "SELECT name, start_time FROM videos ORDER BY start_time"
                )
        }
)
@NamedQueries(
        {
                @NamedQuery(name = "Video.findAll", query = "SELECT v FROM Video v ORDER BY v.start"),
                @NamedQuery(name = "Video.findByName", query = "SELECT v FROM Video v WHERE v.name = :name"),
                @NamedQuery(name = "Video.findByUUID", query = "SELECT v FROM Video v WHERE v.uuid = :uuid"),
                @NamedQuery(
                        name = "Video.findByVideoReferenceUUID",
                        query = "SELECT v FROM Video v LEFT JOIN v.videoReferences w WHERE w.uuid = :uuid"
                ),
                @NamedQuery(
                        name = "Video.findByVideoSequenceUUID",
                        query = "SELECT v FROM Video v JOIN v.videoSequence w WHERE w.uuid = :uuid"
                ),
                @NamedQuery(
                        name = "Video.findBetweenDates",
                        query = "SELECT v FROM Video v WHERE v.start >= :startDate AND v.start <= :endDate"
                )
        }
)
public class VideoEntity implements PersistentObject {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @Column(name = "uuid", nullable = false, updatable = false, length = 36)
    @Convert(converter = UUIDConverter.class)
    UUID uuid;

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 512, unique = true)
    String name;

//    @Expose(serialize = true)
//    @SerializedName(value = "start_timestamp")
    @Basic(optional = false)
    @Column(name = "start_time", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    @Convert(converter = InstantConverter.class)
    Instant start;

//    @Expose(serialize = true)
//    @SerializedName(value = "duration_millis")
    @Column(name = "duration_millis", nullable = true)
    Duration duration;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH}, optional = false)
    @JoinColumn(name = "video_sequence_uuid", nullable = false)
    VideoSequenceEntity videoSequence;

    @Column(name = "description", length = 2048)
    String description;

    @Version
    @Column(name = "last_updated_time")
    protected Timestamp lastUpdatedTime;

//    @SerializedName(value = "video_references")
    @OneToMany(
            targetEntity = VideoReferenceEntity.class,
            cascade = {CascadeType.ALL},
            fetch = FetchType.EAGER,
            mappedBy = "video",
            orphanRemoval = true
    )
    protected List<VideoReferenceEntity> videoReferences = new ArrayList<>();

    public VideoEntity(String name, Instant start) {
        this.name = name;
        this.start = start;
    }

    public VideoEntity(String name, Instant start, Duration duration) {
        this.name = name;
        this.start = start;
        this.duration = duration;
    }

    public VideoEntity(String name, Instant start, Iterable<VideoReferenceEntity> videoRefs) {
        this.name = name;
        this.start = start;
        videoRefs.forEach(this::addVideoReference);
    }

    public VideoEntity(String name, Instant start, Duration duration, Iterable<VideoReferenceEntity> videoRefs) {
        this.name = name;
        this.start = start;
        this.duration = duration;
        videoRefs.forEach(this::addVideoReference);
    }

    public VideoEntity(String name, Instant start, Duration duration, String description) {
        this.description = description;
        this.name = name;
        this.start = start;
        this.duration = duration;
    }

    public VideoEntity() {

    }

    public List<VideoReferenceEntity> getVideoReferences() {
        return videoReferences;
    }

    public void addVideoReference(VideoReferenceEntity videoReference) {
        videoReferences.add(videoReference);
        videoReference.setVideo(this);
    }

    public void removeVideoReference(VideoReferenceEntity videoReference) {
        videoReferences.remove(videoReference);
        videoReference.setVideo(null);
    }

    public VideoSequenceEntity getVideoSequence() {
        return videoSequence;
    }

    public void setVideoSequence(VideoSequenceEntity videoSequence) {
        this.videoSequence = videoSequence;
    }

    @Override
    public Option<UUID> primaryKey() {
        return Option.apply(uuid);
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

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Timestamp getLastUpdatedTime() {
        return lastUpdatedTime;
    }
}
