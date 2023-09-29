/*
 * Copyright 2021 MBARI
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

import org.mbari.vampiresquid.etc.jpa.ByteArrayConverter;
import org.mbari.vampiresquid.etc.jpa.TransactionLogger;
import org.mbari.vampiresquid.etc.jpa.URIConverter;
import org.mbari.vampiresquid.etc.jpa.UUIDConverter;

import jakarta.persistence.*;
import java.net.URI;
import java.sql.Timestamp;
import java.util.UUID;

@Entity(name = "VideoReference")
@Table(
        name = "video_references",
        indexes = {
                @Index(name = "idx_video_references__uri", columnList = "uri"),
                @Index(name = "idx_video_references__video_uuid", columnList = "video_uuid")
        }
)
@EntityListeners({TransactionLogger.class})
@NamedNativeQueries(
        {
                // @NamedNativeQuery(
                //         name = "VideoReference.findByFileName",
                //         query = "SELECT uuid FROM video_references WHERE uri LIKE ?1"
                // ),
                @NamedNativeQuery(
                        name = "VideoReference.findAllURIs",
                        query = "SELECT uri FROM video_references"
                )
        }
)
@NamedQueries(
        {
                @NamedQuery(name = "VideoReference.findAll", query = "SELECT v FROM VideoReference v"),
                @NamedQuery(
                        name = "VideoReference.findBySha512",
                        query = "SELECT v FROM VideoReference v WHERE v.sha512 = :sha512"
                ),
                @NamedQuery(
                        name = "VideoReference.findByVideoUUID",
                        query = "SELECT v FROM VideoReference v JOIN v.video w WHERE w.uuid = :uuid"
                ),
                @NamedQuery(
                        name = "VideoReference.findByURI",
                        query = "SELECT v FROM VideoReference v WHERE v.uri = :uri"
                ),
                @NamedQuery(
                        name = "VideoReference.findByFileName",
                        query = "SELECT v FROM VideoReference v WHERE CAST(v.uri as string) LIKE :filename"
                ),
        }
)
public class VideoReferenceEntity implements IPersistentObject {


    @Id
    // @GeneratedValue(generator = "system-uuid")
    @Column(name = "uuid", nullable = false, updatable = false, length = 36)
    // @Convert(converter = UUIDConverter.class)
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID uuid;

//    def primaryKey: Option[UUID] = Option(uuid)

//    def lastUpdated: Option[Instant] = Option(lastUpdatedTime).map(_.toInstant)


    @Basic(optional = false)
    @Column(name = "uri", unique = true, length = 1024, nullable = false)
    @Convert(converter = URIConverter.class)
    URI uri;

    /**
     * Defines the video files container. We are using mimetypes to provide
     * container definitions. Note that the mimetype does not always indicate
     * the video/audio encoding
     */
    @Column(name = "container", length = 128)
    String container;

    @Column(name = "video_codec", length = 128)
    String videoCodec;

    @Column(name = "audio_codec", length = 128)
    String audioCodec;

    @Column(name = "width")
    Integer width;

    @Column(name = "height")
    Integer height;

    @Column(name = "frame_rate")
    Double frameRate;

//    @Expose(serialize = true)
//    @SerializedName(value = "size_bytes")
    @Column(name = "size_bytes")
    Long size;

//    def mimetype: Option[MimeType] = Try(new MimeType(container)).toOption

    // Checksum allows reverse lookups. Store checksum as hex
    // Ideally this would be a unique key. But we can't make it unique as
    // tapes and real-time sessions use null. Unique would disallow more than one null.
    @Column(name = "sha512", length = 128, nullable = true)
    @Convert(converter = ByteArrayConverter.class)
    byte[] sha512;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.DETACH}, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "video_uuid", nullable = false)
    VideoEntity video;

    @Column(name = "description", length = 2048)
    String description;

    /** Optimistic lock to prevent concurrent overwrites */
    @Version
    @Column(name = "last_updated_time")
    protected Timestamp lastUpdatedTime;

    public VideoReferenceEntity() {

    }

    public VideoReferenceEntity(URI uri) {
        this.uri = uri;
    }

    public VideoReferenceEntity(URI uri, String container,
        String videoCodec,
        String audioCodec) {
            this.uri = uri;
            this.container = container;
            this.videoCodec = videoCodec;
            this.audioCodec = audioCodec;
        }

    public VideoReferenceEntity(URI uri, 
        String container,
        String videoCodec,
        String audioCodec,
        Integer width,
        Integer height,
        Double frameRate,
        Long sizeBytes,
        String description) {
            this.uri = uri;
            this.container = container;
            this.videoCodec = videoCodec;
            this.audioCodec = audioCodec;
            this.width = width;
            this.height = height;
            this.frameRate = frameRate;
            this.size = sizeBytes;
            this.description = description;
        }

        public VideoReferenceEntity(URI uri, 
        String container,
        String videoCodec,
        String audioCodec,
        Integer width,
        Integer height,
        Double frameRate,
        Long sizeBytes,
        String description,
        byte[] sha512) {
            this.uri = uri;
            this.container = container;
            this.videoCodec = videoCodec;
            this.audioCodec = audioCodec;
            this.width = width;
            this.height = height;
            this.frameRate = frameRate;
            this.size = sizeBytes;
            this.description = description;
            this.sha512 = sha512;
        }

    public VideoEntity getVideo() {
        return video;
    }

    public void setVideo(VideoEntity video) {
        this.video = video;
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

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public void setVideoCodec(String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public String getAudioCodec() {
        return audioCodec;
    }

    public void setAudioCodec(String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Double getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(Double frameRate) {
        this.frameRate = frameRate;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public byte[] getSha512() {
        return sha512;
    }

    public void setSha512(byte[] sha512) {
        this.sha512 = sha512;
    }

    public Timestamp getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    @Override
    public String toString() {
        return "VideoReferenceEntity{" +
                "uuid=" + uuid +
                ", uri=" + uri +
                '}';
    }
}
