ALTER TABLE videos DROP CONSTRAINT vdsvdosequenceuuid
ALTER TABLE video_references DROP CONSTRAINT vdreferencesvduuid
DROP INDEX idx_video_sequences__name
DROP INDEX idx_video_sequences__camera_id
DROP TABLE video_sequences
DROP INDEX idx_videos__name
DROP INDEX idx_videos__start_time
DROP INDEX idx_videos__video_sequence_uuid
DROP TABLE videos
DROP INDEX idx_video_references__uri
DROP INDEX idx_video_references__video_uuid
DROP TABLE video_references
