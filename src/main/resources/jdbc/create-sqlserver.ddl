-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE video_sequences (
  uuid                 VARCHAR(72) NOT NULL PRIMARY KEY,
  name                 VARCHAR(512) NOT NULL,
  camera_id            VARCHAR(256) NOT NULL,
  last_updated_time    DATETIME NULL
)
GO

CREATE TABLE videos (
  uuid                 VARCHAR(72) NOT NULL PRIMARY KEY,
  name                 VARCHAR(512) NOT NULL ,
  start_time           DATETIME NULL,
  duration_millis      FLOAT NULL,
  video_sequence_uuid  VARCHAR(72) NOT NULL,
  last_updated_time    DATETIME NULL
)
GO


CREATE TABLE video_references (
  uuid                 VARCHAR(72) NOT NULL PRIMARY KEY,
  uri                  VARCHAR(2048) NOT NULL,
  container            VARCHAR(128) NULL,
  video_codec          VARCHAR(128) NULL,
  audio_codec          VARCHAR(128) NULL,
  width                SMALLINT NULL,
  height               SMALLINT NULL,
  frame_rate           FLOAT NULL,
  size_bytes           BIGINT NULL,
  sha512               VARCHAR(128) null,
  video_uuid           VARCHAR(72) NOT NULL,
  last_updated_time    DATETIME NULL
)
GO

ALTER TABLE video_sequences
    ADD CONSTRAINT uc_videos_sequences__name UNIQUE (name)
GO

ALTER TABLE videos
  ADD CONSTRAINT uc_videos__name UNIQUE (name)
GO

ALTER TABLE videos
  ADD CONSTRAINT fk_videos__video_sequences FOREIGN KEY (uuid)
  REFERENCES video_sequences(uuid)
  ON DELETE CASCADE
  ON UPDATE CASCADE
GO

ALTER TABLE video_references
  ADD CONSTRAINT uc_video_references__uri UNIQUE (uri)
GO

ALTER TABLE video_references
  ADD CONSTRAINT uc_video_references__sha512 UNIQUE (sha512)
GO

ALTER TABLE video_references
  ADD CONSTRAINT fk_video_references__videos FOREIGN KEY (uuid)
  REFERENCES video_sequences(uuid)
  ON DELETE CASCADE
  ON UPDATE CASCADE
GO

CREATE NONCLUSTERED INDEX idx_video_sequences__name
  ON video_sequences(name)
GO

CREATE NONCLUSTERED INDEX idx_video_sequences__camera_id
  ON video_sequences(camera_id)
GO

CREATE NONCLUSTERED INDEX idx_videos__video_sequence_uuid
  ON videos(video_sequence_uuid)
GO

CREATE NONCLUSTERED INDEX idx_videos__name
  ON videos(name)
GO

CREATE NONCLUSTERED INDEX idx_videos__start_time
  ON videos(start_time)
GO

CREATE NONCLUSTERED INDEX idx_video_references__uri
  ON video_references(uri)
GO

CREATE NONCLUSTERED INDEX idx_video_references__video_uuid
  ON video_references(video_uuid)
GO

