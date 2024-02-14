![MBARI logo](src/site/images/logo-mbari-3b.png)

# vampire-squid

![Build](https://github.com/mbari-org/vampire-squid/actions/workflows/scala.yml/badge.svg)

Vampire-squid is a video asset manager.

## tl;dr

A web-service for creating managing videos from mission based camera deployments. Swagger docs are available in your instance at `http://yourhostname.domain:<port>/docs`. __Here's an example of how to launch it using Docker:__

```bash
docker run -d \
    -p 8080:8080 \
    -e BASICJWT_CLIENT_SECRET="xxxx" \
    -e BASICJWT_SIGNING_SECRET="xxxx" \
    -e DATABASE_DRIVER="com.microsoft.sqlserver.jdbc.SQLServerDriver" \
    -e DATABASE_LOG_LEVEL=INFO \
    -e DATABASE_PASSWORD="xxx" \
    -e DATABASE_URL="jdbc:sqlserver://database.mbari.org:1433;databaseName=M3_ANNOTATIONS" \
    -e DATABASE_USER=dbuser \
    -e LOGBACK_LEVEL=WARN \
    --name=vampire-squid \
    --restart unless-stopped \
    mbari/vampire-squid
```

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

