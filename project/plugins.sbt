// Testframework
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// Lifecycle management
addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.1.13")

// Protobuf compiler
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.13")
// FIXME find a way to get version from Versions object
libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.2"

// Packaging
// addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.15")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")