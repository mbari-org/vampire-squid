// -- Plugin Settings -------------------------------------------------

// -- sbt-native-packager
enablePlugins(JavaServerAppPackaging)

// -- xsbt-web-plugin
enablePlugins(JettyPlugin)

mainClass in assembly := Some("JettyMain")