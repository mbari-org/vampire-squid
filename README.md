# video-asset-manager

Currently, this a project for testing ideas and models for managing video and image assets. The basic data models is:

```
VideoSequence [1]--->[0..*] Video [1]--->[0..*] VideoReference 
```

Where:

- `VideoSequence` is essentially a single deployment/session from a single camera. Analagous to a _dive_. Typically, a deployment is broken into segments, e.g. 5 minutes, in order to make the files sizes manageable. This seems to be the current practice among all the groups at the underwater video workshop in RI. 
- `Video` is an abstraction that refers to a single segment in the _VideoSequence_. It tracks the start date and duration of a segment of video. It's an abstraction as it does not point directly to a video via a URL or path as there will likely be multiple representations of the same video segement (such as digital master, mezzanine, and various proxies)
- `VideoReference` is a concrete reference to a representation of a _Video_. It stores the particulars of a file needed to locate a video. It encapsulates the codecs, containers, size and location of a video file.

## Data store

The current data storage targets are SQL Databases. Pretty much all database servers are supported. You can configure the database info in the application.conf file. The database schema will be auto-generated the first time you run the application.

## Build and Run

To build: `sbt pack`

To run:

```
cd target/pack/bin
jetty-main
```

## Design

The design of the _video-asset_manager_ follows the ideas of microservices:

- __It's small and does one thing.__ Here, we are managing the location of segments of video that comprise a camera deployment and providing search services to facilitate finding the file we want.
- __It owns it's own data.__ The current design is to use an SQL database. You can use any database you want. If you decide to use something else later on (MongoDB, Cassandra, whatever) that's OK too (but you have to code it).
- __Access is through standard API.__ Users interact through HTTP/JSON. If we rip out and replace the backend with some other storage, the change will be invisible to all applications that use this service.
- __Independantly isolated and scalable__. If needed, multiple instances could be deployed behind a load balancer.

## API

## TODO

- [ ] finish swagger documentation
- [ ] add docker container
- [ ] document using your own database




This project is built using [SBT](http://www.scala-sbt.org/). See SBT.md for help.



