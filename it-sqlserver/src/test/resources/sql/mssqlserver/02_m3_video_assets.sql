CREATE TABLE "dbo"."video_references"  ( 
	"uuid"             	uniqueidentifier NOT NULL,
	"audio_codec"      	varchar(128) NULL,
	"container"        	varchar(128) NULL,
	"description"      	varchar(2048) NULL,
	"frame_rate"       	float NULL,
	"height"           	int NULL,
	"last_updated_time"	datetime2 NULL,
	"sha512"           	varchar(128) NULL,
	"size_bytes"       	bigint NULL,
	"uri"              	varchar(1024) NOT NULL,
	"video_codec"      	varchar(128) NULL,
	"width"            	int NULL,
	"video_uuid"       	uniqueidentifier NOT NULL,
	CONSTRAINT "PK_video_references__uuid" PRIMARY KEY CLUSTERED("uuid")
 ON [PRIMARY]);

CREATE TABLE "dbo"."video_sequences"  ( 
	"uuid"             	uniqueidentifier NOT NULL,
	"camera_id"        	varchar(256) NOT NULL,
	"description"      	varchar(2048) NULL,
	"last_updated_time"	datetime2 NULL,
	"name"             	varchar(512) NOT NULL,
	CONSTRAINT "PK_video_sequences__uuid" PRIMARY KEY CLUSTERED("uuid")
 ON [PRIMARY]);

CREATE TABLE "dbo"."videos"  ( 
	"uuid"               	uniqueidentifier NOT NULL,
	"description"        	varchar(2048) NULL,
	"duration_millis"    	bigint NULL,
	"last_updated_time"  	datetime2 NULL,
	"name"               	varchar(512) NOT NULL,
	-- "start_time"         	datetime2(6) NOT NULL,
	"start_time"         	datetimeoffset(6) NOT NULL,
	"video_sequence_uuid"	uniqueidentifier NOT NULL,
	CONSTRAINT "PK__videos__uuid" PRIMARY KEY CLUSTERED("uuid")
 ON [PRIMARY]);

CREATE VIEW "dbo"."unique_videos"
AS 
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
    video_sequences AS s LEFT JOIN
    videos AS v ON v.video_sequence_uuid = s.uuid LEFT JOIN
    video_references AS r ON r.video_uuid = v.uuid;

CREATE NONCLUSTERED INDEX "IDX__video_references__uri"
	ON "dbo"."video_references"("uri");

CREATE NONCLUSTERED INDEX "IDX__video_references__video_uuid"
	ON "dbo"."video_references"("video_uuid");

CREATE NONCLUSTERED INDEX "IDX__video_sequences__camera_id"
	ON "dbo"."video_sequences"("camera_id");

CREATE NONCLUSTERED INDEX "IDX__video_sequences__name"
	ON "dbo"."video_sequences"("name");

CREATE NONCLUSTERED INDEX "IDX__videos__name"
	ON "dbo"."videos"("name");

CREATE NONCLUSTERED INDEX "IDX__videos__start_time"
	ON "dbo"."videos"("start_time");

CREATE NONCLUSTERED INDEX "IDX__videos__video_sequence_uuid"
	ON "dbo"."videos"("video_sequence_uuid");

CREATE UNIQUE NONCLUSTERED INDEX "UIDX__video_references__sha512"
	ON "dbo"."video_references"("sha512")
	WHERE ([sha512] IS NOT NULL);

ALTER TABLE "dbo"."video_references"
	ADD CONSTRAINT "UQ__video_references__uri"
	UNIQUE ("uri") 
	WITH (
		DATA_COMPRESSION = NONE
	) ON [PRIMARY];

ALTER TABLE "dbo"."video_sequences"
	ADD CONSTRAINT "UQ__video_sequences__name"
	UNIQUE ("name") 
	WITH (
		DATA_COMPRESSION = NONE
	) ON [PRIMARY];

ALTER TABLE "dbo"."videos"
	ADD CONSTRAINT "UQ__videos__name"
	UNIQUE ("name")
	WITH (
		DATA_COMPRESSION = NONE
	) ON [PRIMARY];

ALTER TABLE "dbo"."video_references"
	ADD CONSTRAINT "FK__video_references__video__uuid"
	FOREIGN KEY("video_uuid")
	REFERENCES "dbo"."videos"("uuid")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION ;

ALTER TABLE "dbo"."videos"
	ADD CONSTRAINT "FK__videos__video_sequences__uuid"
	FOREIGN KEY("video_sequence_uuid")
	REFERENCES "dbo"."video_sequences"("uuid")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION ;

CREATE TABLE REVINFO (
  REV int identity NOT NULL, 
  REVTSTMP bigint, 
  PRIMARY KEY (REV)
);

CREATE TABLE video_sequences_AUD (
  REV int not null, 
  REVTYPE smallint, 
  uuid uniqueidentifier not null, 
  camera_id varchar(256), 
  name varchar(512), 
  description varchar(2048), 
  primary key (REV, uuid)
);
ALTER TABLE video_sequences_AUD 
  ADD CONSTRAINT "FK__video_seqs_aud__revinfo" FOREIGN KEY (REV) references REVINFO;

CREATE TABLE videos_AUD (
  REV int not null, 
  REVTYPE smallint, 
  duration_millis bigint, 
  start_time datetimeoffset(6),
  uuid uniqueidentifier not null, 
  video_sequence_uuid uniqueidentifier, 
  name varchar(512), 
  description varchar(2048), 
  primary key (REV, uuid)
);
ALTER TABLE videos_AUD 
  ADD CONSTRAINT "FK__videos_aud__revinfo" FOREIGN KEY (REV) REFERENCES REVINFO;


CREATE TABLE video_references_AUD (
  REV int not null, 
  REVTYPE smallint, 
  frame_rate float(53), 
  height int, 
  width int, 
  size_bytes bigint, 
  uuid uniqueidentifier not null, 
  video_uuid uniqueidentifier, 
  audio_codec varchar(128), 
  container varchar(128), 
  sha512 varchar(128), 
  video_codec varchar(128), 
  uri varchar(1024), 
  description varchar(2048), 
  primary key (REV, uuid)
);
ALTER TABLE video_references_AUD 
  ADD CONSTRAINT "FK__video_refs_aud__revinfo" FOREIGN KEY (REV) REFERENCES REVINFO;



