CREATE TABLE video_sequence (
  uuid                 VARCHAR(72) NOT NULL PRIMARY KEY,
  name                 VARCHAR(512) NOT NULL,
  camera_id            VARCHAR(256) NOT NULL,
  last_updated_time    DATETIME NULL
)
GO

CREATE TABLE video (
  uuid                 VARCHAR(72) NOT NULL PRIMARY KEY,
  name                 VARCHAR(512) NOT NULL ,
  start_time           DATETIME NULL,
  duration_seconds     FLOAT NULL,
  video_sequence_uuid  VARCHAR(72) NOT NULL,
  last_updated_time    DATETIME NULL
)
GO


CREATE TABLE video_view (
  uuid                 VARCHAR(72) NOT NULL PRIMARY KEY,
  uri                  VARCHAR(2048) NOT NULL,
  container            VARCHAR(128) NULL,
  video_codec          VARCHAR(128) NULL,
  audio_codec          VARCHAR(128) NULL,
  width                SMALLINT NULL,
  height               SMALLINT NULL,
  video_uuid           VARCHAR(72) NOT NULL,
  last_updated_time    DATETIME NULL
)
GO

ALTER TABLE video_sequence
    ADD CONSTRAINT uc_video_sequence__name UNIQUE (name)
GO

ALTER TABLE video
  ADD CONSTRAINT uc_video__name UNIQUE (name)
GO

ALTER TABLE video
  ADD CONSTRAINT fk_video__video_sequence FOREIGN KEY (uuid)
  REFERENCES video_sequence(uuid)
  ON DELETE CASCADE
  ON UPDATE CASCADE
GO

ALTER TABLE video_view
  ADD CONSTRAINT uc_video_view__uri UNIQUE (uri)
GO

ALTER TABLE video_view
  ADD CONSTRAINT fk_video_view__video FOREIGN KEY (uuid)
  REFERENCES video_sequence(uuid)
  ON DELETE CASCADE
  ON UPDATE CASCADE
GO

CREATE NONCLUSTERED INDEX idx_video_sequence__name
  ON video_sequence(name)
GO

CREATE NONCLUSTERED INDEX idx_video_sequence__camera_id
  ON video_sequence(camera_id)
GO

CREATE NONCLUSTERED INDEX idx_video__video_sequence_uuid
  ON video(video_sequence_uuid)
GO

CREATE NONCLUSTERED INDEX idx_video__name
  ON video(name)
GO

CREATE NONCLUSTERED INDEX idx_video_view__uri
  ON video_view(uri)
GO

CREATE NONCLUSTERED INDEX idx_video_view__video_uuid
  ON video_view(video_uuid)
GO