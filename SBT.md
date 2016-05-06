# SBT - Simple Build Tool

## Useful [SBT commands](http://www.scala-sbt.org/release/docs/Command-Line-Reference.html) for this project

- `checkVersions`: Show dependency updates
- `clean`
- `cleanall`: Does `clean` and `clean-files`
- `compile` or `~compile` (continuous)
- `console`: Opens a scala console that includes the projects dependencies and code on the classpath
- `dependency-tree`: Shows an ASCII dependency graph
- `dependencyList`: Prints a flat, deduplicated list of all the transitive dependencies.
- `dependencyStats`: Prints a simple table of jar sizes for all your dependencies. Handy if you want to know why your assembled jar gets so big.
- `dependencyUpdates`: Show dependency updates
- `doc`: Generate Scaladoc into target/api
- `export fullClasspath`: Generate the classpath needed to run the project
- `install`
- `ivy-report`: build a report of dependencies using ivy in XML (viewable in a browser)
- `lint:compile`: Run static checkers as part of compilion. (Static checking is slow)
- `offline`: Use SBT offline
- `pack`: Builds a standalone distribution of this project under `target/pack`
- `pack-archive`: Takes the product from `pack` and generates a _tar.gz_ archive
- `package`: Creates the main artifact (e.g. a jar) under `target`
- `publish-local` or `~publish-local` (continous): Publish to the local ivy repo
- `publishM2`: Publish to the local maven repo
- `reload`: Reloads the build. Useful if you edit build.sbt.
_ `scalastyleGenerateConfig`: Generates a scalastyle config file. Run before using `scalastyle`
- `scalastyle`: Checks code style. Results go into target/scalastyle-result.xml. Also `test:scalastyle`
- `show ivy-report`: Show the location of the dependency report
- `show update`: Show dependencies and indicate which were evicted
- `tasks -V`: Shows all available tasks/commands
- `test` or `~test` (continuous)
- `todos`: Display a listing of all TODO, FIXME, WIP, or XXX comments
- `update-classifiers`: Download sources and javadoc for all dependencies
- `version-report`: Shows a flat listing of all dependencies in this project, including transitive ones.
