-- Create the extension if not exists
-- (Oracle doesn't have UUID data type, so you'll need to use RAW(16) for UUID)
-- (You can create the UUID functionality using RAW and UTL_RAW package)
-- However, you might want to use VARCHAR2(36) or RAW(16) as a workaround.

-- For example, in Oracle:
-- CREATE OR REPLACE FUNCTION uuid_generate_v4 RETURN VARCHAR2 IS
--   uuid_str VARCHAR2(36);
-- BEGIN
--   uuid_str := SYS_GUID();
--   RETURN uuid_str;
-- END;
-- /

CREATE TABLE video_sequences (
  uuid RAW(16) PRIMARY KEY NOT NULL,
  camera_id VARCHAR2(256) NOT NULL,
  description VARCHAR2(2048),
  name VARCHAR2(512) NOT NULL,
  last_updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT unique__video_sequence__name UNIQUE (name)
);

CREATE TABLE videos (
  uuid RAW(16) PRIMARY KEY NOT NULL,
  video_sequence_uuid RAW(16) NOT NULL,
  description VARCHAR2(2048),
  duration_millis NUMBER(19, 0),
  name VARCHAR2(512) NOT NULL,
  start_time TIMESTAMP NOT NULL,
  last_updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk__videos__video_sequences FOREIGN KEY (video_sequence_uuid) REFERENCES video_sequences (uuid),
  CONSTRAINT unique__videos__name UNIQUE (name)
);

CREATE TABLE video_references (
  uuid RAW(16) PRIMARY KEY NOT NULL,
  video_uuid RAW(16) NOT NULL,
  audio_codec VARCHAR2(128),
  container VARCHAR2(128),
  description VARCHAR2(2048),
  frame_rate float(53),
  height NUMBER,
  sha512 VARCHAR2(128),
  size_bytes NUMBER(19, 0),
  uri VARCHAR2(1024) NOT NULL,
  video_codec VARCHAR2(128),
  width NUMBER,
  last_updated_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT unique__video_references__uri UNIQUE (uri),
  CONSTRAINT fk__video_references__videos FOREIGN KEY (video_uuid) REFERENCES videos (uuid)
);

-- CREATE INDEX idx_video_references__uri ON video_references(uri);
CREATE INDEX idx_video_references__video_uuid ON video_references(video_uuid);
CREATE INDEX idx_video_sequences__camera_id ON video_sequences(camera_id);
-- CREATE INDEX idx_video_sequences__name ON video_sequences(name);
-- CREATE INDEX idx_videos__name ON videos(name);
CREATE INDEX idx_videos__start_time ON videos(start_time);
CREATE INDEX idx_videos__video_sequence_uuid ON videos(video_sequence_uuid);

-- Create the view in Oracle (using ANSI SQL JOIN syntax)
CREATE OR REPLACE VIEW unique_videos AS
SELECT
  s.uuid AS video_sequence_uuid,
  s.name AS video_sequence_name,
  v.uuid AS video_uuid,
  v.name AS video_name,
  r.uuid AS video_reference_uuid,
  r.uri,
  s.camera_id,
  v.duration_millis,
  v.start_time,
  r.container,
  r.width,
  r.height,
  r.size_bytes,
  r.last_updated_time,
  r.sha512
FROM
  video_sequences s
  LEFT JOIN videos v ON v.video_sequence_uuid = s.uuid
  LEFT JOIN video_references r ON r.video_uuid = v.uuid;
