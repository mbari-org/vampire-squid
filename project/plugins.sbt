addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"                 % "0.9.26")
addSbtPlugin("com.earldouglas"   % "xsbt-web-plugin"              % "4.1.0")
addSbtPlugin("com.github.atais"  % "sbt-eclipselink-static-weave" % "0.1.1")
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"                  % "0.5.1")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"                      % "1.0.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header"                   % "5.6.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"                 % "2.4.2")
addSbtPlugin("org.scalastyle"    %% "scalastyle-sbt-plugin"       % "1.0.0")
addSbtPlugin("org.xerial.sbt"    % "sbt-pack"                     % "0.13")

resolvers += Resolver.sonatypeRepo("releases")
