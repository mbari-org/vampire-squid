# How to setup your database

It's very simple to use your own SQL Database. The following databases are explicitly supported:

- Attunity
- Auto, Database
- Cloudscape, Derby, JavaDB
- DB2, DB2MainFrame
- DBase
- H2 (Set "org.mbari.vars.vam.database.production.name" in your config to "Database")
- HANA
- HSQL
- Informix, Informix11
- MaxDB
- MySQL4, MySQL
- Oracle, Oracle11, Oracle10g, Oracle9i, Oracle8i
- PointBase,
- PostgreSQL
- SQLServer
- Sybase
- Symfoware
- timesTen


## Building 

To setup a database you just need a build of _video-asset-manager_. If you're building from source you will need to install [GIT](https://git-scm.com/) and  [SBT](http://www.scala-sbt.org/). To build:

```
git clone https://github.com/underwatervideo/video-asset-manager.git
cd video-asset-manager
sbt pack
```

The built code will bin in `video-asset-manager/target/pack`.


## Configure for your database

1. You will need to find the JDBC driver for your database. These are normally distributed as `jar` files. Download the driver jar file and copy it into `video-asset-manager/target/pack/lib`. 
2. 

