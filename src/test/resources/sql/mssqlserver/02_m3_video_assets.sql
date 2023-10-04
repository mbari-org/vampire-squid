CREATE TABLE "dbo"."video_references"  ( 
	"uuid"             	varchar(255) NOT NULL,
	"audio_codec"      	varchar(128) NULL,
	"container"        	varchar(128) NULL,
	"description"      	varchar(2048) NULL,
	"frame_rate"       	float NULL,
	"height"           	int NULL,
	"last_updated_time"	datetime2 NULL,
	"sha512"           	varchar(128) NULL,
	"size_bytes"       	numeric(19,0) NULL,
	"uri"              	varchar(1024) NOT NULL,
	"video_codec"      	varchar(128) NULL,
	"width"            	int NULL,
	"video_uuid"       	varchar(255) NOT NULL,
	"rowguid"          	uniqueidentifier ROWGUIDCOL NOT NULL CONSTRAINT "MSmerge_df_rowguid_588B50A09EE94860B9B5F65FF1D825ED"  DEFAULT (newsequentialid()),
	CONSTRAINT "PK__video_re__7F427930D0AE73A2" PRIMARY KEY CLUSTERED("uuid")
 ON [PRIMARY]);
CREATE TABLE "dbo"."video_sequences"  ( 
	"uuid"             	varchar(255) NOT NULL,
	"camera_id"        	varchar(256) NOT NULL,
	"description"      	varchar(2048) NULL,
	"last_updated_time"	datetime2 NULL,
	"name"             	varchar(512) NOT NULL,
	"rowguid"          	uniqueidentifier ROWGUIDCOL NOT NULL CONSTRAINT "MSmerge_df_rowguid_642CAB49C27A4CF9B146188E8D657686"  DEFAULT (newsequentialid()),
	CONSTRAINT "PK__video_se__7F4279308D60CFC3" PRIMARY KEY CLUSTERED("uuid")
 ON [PRIMARY]);
CREATE TABLE "dbo"."videos"  ( 
	"uuid"               	varchar(255) NOT NULL,
	"description"        	varchar(2048) NULL,
	"duration_millis"    	numeric(19,0) NULL,
	"last_updated_time"  	datetime2 NULL,
	"name"               	varchar(512) NOT NULL,
	"start_time"         	datetime2 NOT NULL,
	"video_sequence_uuid"	varchar(255) NOT NULL,
	"rowguid"            	uniqueidentifier ROWGUIDCOL NOT NULL CONSTRAINT "MSmerge_df_rowguid_94715AE17496492A874E2133A870E1E5"  DEFAULT (newsequentialid()),
	CONSTRAINT "PK__videos__7F427930696ABA50" PRIMARY KEY CLUSTERED("uuid")
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
CREATE NONCLUSTERED INDEX "idx_video_references__uri"
	ON "dbo"."video_references"("uri");
CREATE NONCLUSTERED INDEX "idx_video_references__video_uuid"
	ON "dbo"."video_references"("video_uuid");
CREATE NONCLUSTERED INDEX "idx_video_sequences__camera_id"
	ON "dbo"."video_sequences"("camera_id");
CREATE NONCLUSTERED INDEX "idx_video_sequences__name"
	ON "dbo"."video_sequences"("name");
CREATE NONCLUSTERED INDEX "idx_videos__name"
	ON "dbo"."videos"("name");
CREATE NONCLUSTERED INDEX "idx_videos__start_time"
	ON "dbo"."videos"("start_time");
CREATE NONCLUSTERED INDEX "idx_videos__video_sequence_uuid"
	ON "dbo"."videos"("video_sequence_uuid");
CREATE UNIQUE NONCLUSTERED INDEX "uc_video_references___sha512"
	ON "dbo"."video_references"("sha512")
	WHERE ([sha512] IS NOT NULL);

ALTER TABLE "dbo"."video_references"
	ADD CONSTRAINT "UQ__video_re__DD7784128348399A"
	UNIQUE ("uri") NOT ENFORCED 
	WITH (
		DATA_COMPRESSION = NONE
	) ON [PRIMARY];
ALTER TABLE "dbo"."video_sequences"
	ADD CONSTRAINT "UQ__video_se__72E12F1BC8B7D8A5"
	UNIQUE ("name") NOT ENFORCED 
	WITH (
		DATA_COMPRESSION = NONE
	) ON [PRIMARY];
ALTER TABLE "dbo"."videos"
	ADD CONSTRAINT "UQ__videos__72E12F1B234A88FA"
	UNIQUE ("name") NOT ENFORCED 
	WITH (
		DATA_COMPRESSION = NONE
	) ON [PRIMARY];
ALTER TABLE "dbo"."video_references"
	ADD CONSTRAINT "vdeoreferencesvdeouuid"
	FOREIGN KEY("video_uuid")
	REFERENCES "dbo"."videos"("uuid")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION ;
ALTER TABLE "dbo"."videos"
	ADD CONSTRAINT "vdeosvideosequenceuuid"
	FOREIGN KEY("video_sequence_uuid")
	REFERENCES "dbo"."video_sequences"("uuid")
	ON DELETE NO ACTION 
	ON UPDATE NO ACTION ;
