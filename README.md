# video-asset-manager

Currently, this a project for testing ideas and models for managing video and image assets.

## Data store

The current data storage targets are SQL Databases. Pretty much all database servers are supported. You can configure the database info in the application.conf file. The database schema will be auto-generated the first time you run the application.

## Usage

To build: `sbt pack`

To run:

```
cd target/pack/bin
jetty-main
```

This project is built using [SBT](http://www.scala-sbt.org/). See SBT.md for help.



