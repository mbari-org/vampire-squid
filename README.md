![MBARI logo](vampire-squid/src/site/images/logo-mbari-3b.png)

# vampire-squid

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/mbari-org/vampire-squid) ![Build](https://github.com/mbari-org/vampire-squid/actions/workflows/scala.yml/badge.svg)

Vampire-squid is a video asset manager for tracking videos from mission-based camera deployments.

## Key Features

- **Multi-format Video Tracking**: Manage multiple representations (master, mezzanine, proxies) of video segments with different codecs, resolutions, and containers
- **Temporal Indexing**: Store and query precise timestamp information for each video frame, critical for scientific analysis
- **Hierarchical Organization**: Three-level data model (VideoSequence → Video → VideoReference) maps to real-world deployment workflows
- **RESTful API**: Language-agnostic HTTP API with comprehensive Swagger documentation at `/docs`
- **Multi-Database Support**: Works with PostgreSQL and SQL Server
- **JWT Authentication**: Secure endpoints with JSON Web Token authentication
- **Checksum Verification**: SHA-512 checksums for video file integrity validation
- **Concurrent Video Search**: Find videos recorded simultaneously across different cameras
- **Flyway Migrations**: Automated database schema management and versioning
- **Prometheus Metrics**: Built-in metrics endpoint for monitoring at `/metrics`

## Docker Deployment

Vampire-squid is distributed as a Docker image: `mbari/vampire-squid`

### Quick Start with SQL Server

```bash
docker run -d \
    -p 8080:8080 \
    -e BASICJWT_CLIENT_SECRET="your-client-secret" \
    -e BASICJWT_SIGNING_SECRET="your-signing-secret" \
    -e DATABASE_DRIVER="com.microsoft.sqlserver.jdbc.SQLServerDriver" \
    -e DATABASE_URL="jdbc:sqlserver://your-database-host:1433;databaseName=vampire_squid" \
    -e DATABASE_USER="dbuser" \
    -e DATABASE_PASSWORD="dbpassword" \
    -e DATABASE_LOG_LEVEL=INFO \
    -e LOGBACK_LEVEL=WARN \
    --name=vampire-squid \
    --restart unless-stopped \
    mbari/vampire-squid
```

### PostgreSQL Example

```bash
docker run -d \
    -p 8080:8080 \
    -e BASICJWT_CLIENT_SECRET="your-client-secret" \
    -e BASICJWT_SIGNING_SECRET="your-signing-secret" \
    -e DATABASE_DRIVER="org.postgresql.Driver" \
    -e DATABASE_URL="jdbc:postgresql://your-database-host:5432/vampire_squid" \
    -e DATABASE_USER="dbuser" \
    -e DATABASE_PASSWORD="dbpassword" \
    --name=vampire-squid \
    --restart unless-stopped \
    mbari/vampire-squid
```

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `BASICJWT_CLIENT_SECRET` | Secret for JWT client authentication | `your-client-secret` |
| `BASICJWT_SIGNING_SECRET` | Secret for JWT token signing | `your-signing-secret` |
| `DATABASE_DRIVER` | JDBC driver class name | `org.postgresql.Driver` |
| `DATABASE_URL` | JDBC connection string | `jdbc:postgresql://host:5432/dbname` |
| `DATABASE_USER` | Database username | `dbuser` |
| `DATABASE_PASSWORD` | Database password | `dbpassword` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `HTTP_PORT` | HTTP server port | `8080` |
| `DATABASE_LOG_LEVEL` | Hibernate logging level | `INFO` |
| `LOGBACK_LEVEL` | Application logging level | `INFO` |
| `BASICJWT_ISSUER` | JWT token issuer | `http://www.mbari.org` |

### Docker Compose Example

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: vampire_squid
      POSTGRES_USER: vam_user
      POSTGRES_PASSWORD: vam_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  vampire-squid:
    image: mbari/vampire-squid:latest
    depends_on:
      - postgres
    environment:
      BASICJWT_CLIENT_SECRET: "your-client-secret"
      BASICJWT_SIGNING_SECRET: "your-signing-secret"
      DATABASE_DRIVER: "org.postgresql.Driver"
      DATABASE_URL: "jdbc:postgresql://postgres:5432/vampire_squid"
      DATABASE_USER: "vam_user"
      DATABASE_PASSWORD: "vam_password"
      DATABASE_LOG_LEVEL: "INFO"
    ports:
      - "8080:8080"
    restart: unless-stopped

volumes:
  postgres_data:
```

### Accessing the API

Once running, access:
- **Swagger UI**: `http://localhost:8080/docs`
- **API**: `http://localhost:8080/v1/`
- **Metrics**: `http://localhost:8080/metrics`
- **Health**: `http://localhost:8080/health`

## Overview

The service in this repository is one component of our [Video Annotation and Reference System](https://github.com/mbari-org/m3-quickstart). _vampire-squid_ is a REST-based web service that stores and retrieves information about videos. It is a video asset manager for tracking videos from camera deployments. Typically, the video from a single deployment is chunked into segments (to be small enough to be manageble). Each chunk may have several representations at different resolutions and codecs. Vampire-squid tracks these videos as a unit. It also stores the single most important bit of data needed for scientific research: The date/time that each frame in a video was recorded.

It is designed to work as a programming-language agnostic API that can be accessed from any programming language. The goal of this project is to provide a data service that allows developers and scientists to easily build their own tools for annotating video and images collections. If your video capture looks something remotley like the image below, then this video asset manager may be useful for you:

![Video File Workflow](vampire-squid/src/site/images/digital_videos.png)

See <https://github.com/mbari-media-management/m3-microservices> for a project for spinning up all M3 microservices, in including vampire-squid

## Data Model

The data model is:

```
VideoSequence-[1]---[0..*]->Video-[1]---[0..*]->VideoReference 
```

- `VideoSequence` is essentially a single deployment/session from a single camera. Analagous to a _dive_. Typically, a deployment is broken into segments, e.g. 5 minutes, in order to make the files sizes manageable. This seems to be the current practice among all the groups at the underwater video workshop in RI. 
- `Video` is an abstraction that refers to a single segment in the _VideoSequence_. It tracks the start date and duration of a segment of video. It's an abstraction as it does not point directly to a video via a URL or path as there will likely be multiple representations of the same video segement (such as digital master, mezzanine, and various proxies)
- `VideoReference` is a concrete reference to a representation of a _Video_. It stores the particulars of a file needed to locate a video. It encapsulates the codecs, containers, size and location of a video file.

## Developer stuff

### Quick start

If you don't have [sbt](https://www.scala-sbt.org) installed already, you can use the provided wrapper script:

```shell
./sbtx -h # shows an usage of a wrapper script
./sbtx compile # build the project
./sbtx test # run the tests
./sbtx run # run the application (Main)
```

For more details check the [sbtx usage](https://github.com/dwijnand/sbt-extras#sbt--h) page.

Otherwise, if sbt is already installed, you can use the standard commands:

```shell
sbt compile # build the project
sbt test # run the tests
sbt run # run the application (Main)
```

### Links:

* [tapir documentation](https://tapir.softwaremill.com/en/latest/)
* [tapir github](https://github.com/softwaremill/tapir)
* [bootzooka: template microservice using tapir](https://softwaremill.github.io/bootzooka/)
* [sbtx wrapper](https://github.com/dwijnand/sbt-extras#installation)

### Debugging tests

#### Add munit tag

```scala
val todoTag = new munit.Tag("todo")
test("delete".tag(todoTag)):
  assert(true)
```

Run in sbt as:

```sh
testOnly org.mbari.vampiresquid.repository.jpa.DerbyVideoDAOSuite -- --include-tags=todo
```

