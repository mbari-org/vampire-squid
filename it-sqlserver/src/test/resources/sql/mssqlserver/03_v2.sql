-- Brian Schlining
-- Migration of vampie squid from eclipselink to hibernate

-- video_references: Copy varchar uuids into uniqueidentifiers
alter table dbo.video_references
    add uuid_fixed UNIQUEIDENTIFIER
go
update dbo.video_references
  set uuid_fixed = CONVERT(UNIQUEIDENTIFIER, uuid)
go
alter table dbo.video_references
    add video_uuid_fixed UNIQUEIDENTIFIER
go
update dbo.video_references
  set video_uuid_fixed = CONVERT(UNIQUEIDENTIFIER, uuid)
go

-- videos: Copy varchar uuids into uniqueidentifiers
alter table dbo.videos
    add uuid_fixed UNIQUEIDENTIFIER
go
update dbo.videos
  set uuid_fixed = CONVERT(UNIQUEIDENTIFIER, uuid)
go
alter table dbo.videos
    add video_sequence_uuid_fixed UNIQUEIDENTIFIER
go
update dbo.videos
  set video_sequence_uuid_fixed = CONVERT(UNIQUEIDENTIFIER, video_sequence_uuid)
go

-- videos_sequences: Copy varchar uuids into uniqueidentifiers
alter table dbo.video_sequences
    add uuid_fixed UNIQUEIDENTIFIER
go
update dbo.video_sequences
  set uuid_fixed = CONVERT(UNIQUEIDENTIFIER, uuid)
go
alter table
    dbo.video_references drop constraint PK__video_re__7F427930D0AE73A2
go
alter table
    dbo.video_references drop constraint UQ__video_re__DD7784128348399A
go

-- Drop foreign keys
alter table
    dbo.videos drop constraint vdeosvideosequenceuuid
go
alter table
    dbo.video_references drop constraint vdeoreferencesvdeouuid
go

-- drop indices and contraints on the varchar uuids
drop index idx_video_references__video_uuid on dbo.video_references
go
alter table
    dbo.video_references drop constraint PK__video_re__7F427930D0AE73A2
go
alter table
    dbo.video_references drop constraint UQ__video_re__DD7784128348399A
go
alter table
    dbo.videos drop constraint PK__videos__7F427930696ABA50
go
alter table
    dbo.videos drop constraint UQ__videos__72E12F1B234A88FA
go
drop index idx_videos__video_sequence_uuid on dbo.videos
go
alter table
    dbo.video_sequences drop constraint PK__video_se__7F4279308D60CFC3
go
alter table
    dbo.video_sequences drop constraint UQ__video_se__72E12F1BC8B7D8A5
go

-- video_references: drop the varchar uuids and switch to uniqueidentifiers

alter table dbo.video_references
    alter column uuid_fixed uniqueidentifier not null
go

alter table dbo.video_references
    drop column uuid
go

alter table dbo.video_references
    add constraint PK__video_references__uuid PRIMARY KEY CLUSTERED(uuid_fixed)
go

EXEC sp_rename 'dbo.video_references.uuid_fixed', 'uuid', 'COLUMN'
go

-- -- 

alter table dbo.video_references
    alter column video_uuid_fixed uniqueidentifier not null
go

alter table dbo.video_references
    drop column video_uuid
go

EXEC sp_rename 'dbo.video_references.video_uuid_fixed', 'video_uuid', 'COLUMN'
go


-- videos: drop the varchar uuids and switch to uniqueidentifiers

alter table dbo.videos
    alter column uuid_fixed uniqueidentifier not null
go

alter table dbo.videos
    drop column uuid
go

alter table dbo.videos
    add constraint PK__videos__uuid PRIMARY KEY CLUSTERED(uuid_fixed)
go

EXEC sp_rename 'dbo.videos.uuid_fixed', 'uuid', 'COLUMN'
go

-- -- 

alter table dbo.videos
    alter column video_sequence_uuid_fixed uniqueidentifier not null
go

alter table dbo.videos
    drop column video_sequence_uuid
go

EXEC sp_rename 'dbo.videos.video_sequence_uuid_fixed', 'video_sequence_uuid', 'COLUMN'
go


-- video_sequences: drop the varchar uuids and switch to uniqueidentifiers
alter table dbo.video_sequences
    alter column uuid_fixed uniqueidentifier not null
go

alter table dbo.video_sequences
    drop column uuid
go

alter table dbo.video_sequences
    add constraint PK__video_sequences__uuid PRIMARY KEY CLUSTERED(uuid_fixed)
go

EXEC sp_rename 'dbo.video_sequences.uuid_fixed', 'uuid', 'COLUMN'
go

-- Add foreign keys
ALTER TABLE "dbo"."video_references"
	ADD CONSTRAINT "FK__video_references__video__uuid"
	FOREIGN KEY("video_uuid")
	REFERENCES "dbo"."videos"("uuid")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION
go

ALTER TABLE "dbo"."videos"
	ADD CONSTRAINT "FK__videos__video_sequences__uuid"
	FOREIGN KEY("video_sequence_uuid")
	REFERENCES "dbo"."video_sequences"("uuid")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION
go

-- Fix columns for hibernate
ALTER TABLE dbo.video_references ALTER COLUMN size_bytes bigint
go
ALTER TABLE dbo.videos ALTER COLUMN duration_millis bigint
go
drop index IDX__videos__start_time on dbo.video_references
go
ALTER TABLE dbo.videos ALTER COLUMN start_time datetimeoffset(6)
go
CREATE NONCLUSTERED INDEX "IDX__videos__start_time"
	ON "dbo"."videos"("start_time")
go

CREATE NONCLUSTERED INDEX "IDX__video_references__video_uuid"
	ON "dbo"."video_references"("video_uuid")
go

CREATE NONCLUSTERED INDEX "IDX__videos__video_sequence_uuid"
	ON "dbo"."videos"("video_sequence_uuid")
go

