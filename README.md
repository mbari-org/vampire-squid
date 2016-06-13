![MBARI logo](https://raw.githubusercontent.com/underwatervideo/video-asset-manager/master/src/site/images/logo-mbari-3b.png)

# video-asset-manager

Currently, this a project for testing ideas and models for managing video and image assets. The basic data models is:

```
VideoSequence [1]--->[0..*] Video [1]--->[0..*] VideoReference 
```

The yellow block in this diagram illustrates the design:

![Diagram](https://raw.githubusercontent.com/underwatervideo/video-asset-manager/master/src/site/docs/VideoTAG_data_model.png)

Where:

- `VideoSequence` is essentially a single deployment/session from a single camera. Analagous to a _dive_. Typically, a deployment is broken into segments, e.g. 5 minutes, in order to make the files sizes manageable. This seems to be the current practice among all the groups at the underwater video workshop in RI. 
- `Video` is an abstraction that refers to a single segment in the _VideoSequence_. It tracks the start date and duration of a segment of video. It's an abstraction as it does not point directly to a video via a URL or path as there will likely be multiple representations of the same video segement (such as digital master, mezzanine, and various proxies)
- `VideoReference` is a concrete reference to a representation of a _Video_. It stores the particulars of a file needed to locate a video. It encapsulates the codecs, containers, size and location of a video file.

## Data store

The current data storage targets are SQL Databases. Pretty much all database servers are supported. You can configure the database info in the [application.conf](https://github.com/underwatervideo/video-asset-manager/blob/master/src/pack/conf/application.conf) file. The database schema will be auto-generated the first time you run the application. 

Note that you will need to include you database's JDBC driver. There's a variety of ways to do it but the simplest for non-developers is to drop the driver's jar file in the build's `lib` directory. 

The default setup is to use an in-memory derby database. This is useful for testing and development, but as configured, you lose your data when you stop the application.

## Build and Run

To build: `sbt pack`

To run:

```
cd target/pack/bin
jetty-main
```

You can do a quick test by pointing to the server through a web browser at:
    
    http://localhost:8080/v1/videosequence
    
That will dump your entire database out as JSON. 

## Design

The design of the _video-asset-manager_ follows the ideas of microservices:

- __It's small and does one thing.__ Here, we are managing the location of segments of video that comprise a camera deployment and providing search services to facilitate finding the file we want.
- __It owns its own data.__ The current design is to use an SQL database. You can use any database you want. If you decide to use something else later on (MongoDB, Cassandra, whatever) that's OK too (but you have to code it).
- __Access is through standard API.__ Users interact through HTTP/JSON. If we rip out and replace the backend with some other storage, the change will be invisible to all applications that use this service.
- __Independently isolated and scalable__. If needed, multiple instances could be deployed behind a load balancer.

## API

__Coming soon__. Right now there's an example python script that demo's adding data: [simple_setup.py](https://github.com/underwatervideo/video-asset-manager/blob/master/src/pack/bin/simple_setup.py)

## TODO

- [ ] finish swagger documentation
- [ ] dockerize the project
- [ ] document using your own database
- [ ] Add authentication for `post`, `put`, `delete` methods
- [ ] Add JPA indices




This project is built using [SBT](http://www.scala-sbt.org/). See SBT.md for help.



