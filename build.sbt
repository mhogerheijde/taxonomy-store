import Dependencies._


ThisBuild / organization := "net.hogerheijde.taxonomy-provider"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.8"
ThisBuild / name         := "taxonomy-store"
ThisBuild / libraryDependencies ++= scalatest

// -----------------------------------------------------------------------------
// Compile & Analysis


// See: Toward a safer Scala
// http://downloads.typesafe.com/website/presentations/ScalaDaysSF2015/Toward%20a%20Safer%20Scala%20@%20Scala%20Days%20SF%202015.pdf
lazy val minimalScalacOptions = Seq(
  "-target:jvm-1.8",

  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-unused:imports,privates,locals,implicits",
  "-Xlint:_"
)
lazy val strictScalacOptions = minimalScalacOptions ++ Seq("-Xfatal-warnings")

// We do not want any fatal warnings in the IDE when imports are unused.
// System property lenient is needed to communicate that to this build script.
// In IntelliJ, under SBT settings, you can set VM parameters to contain "-Dlenient=true"

ThisBuild / scalacOptions ++= {
  sys.props.getOrElse("lenient", "false").toBoolean match {
    case true => minimalScalacOptions
    case _ => strictScalacOptions
  }
}

(ThisBuild / scalastyleConfig in Compile) := baseDirectory.value / "scalastyle_config.xml"
(ThisBuild / scalastyleConfig in Test) := baseDirectory.value / "scalastyle_config.xml"



lazy val common = project
  .in(file("common"))
  .disablePlugins(AssemblyPlugin)

lazy val api = project
  .in(file("api"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    name := "api",
    libraryDependencies ++= protobuf,
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
  )


lazy val client = project
  .dependsOn(api, common)
  .disablePlugins(AssemblyPlugin)
  .in(file("client"))
  .settings(
    name := "client",
    libraryDependencies ++= grpc,
    libraryDependencies ++= logging, // FIXME Client should not have logging configured like this
    libraryDependencies ++= Seq(
      tqa,
    ),
  )

lazy val server = project
  .dependsOn(api, common)
  .in(file("server"))
  .enablePlugins(
    UniversalPlugin, // FIXME not sure if we need this one explicitly
    JavaAppPackaging, // Create runnable jar
    DockerPlugin, // Package runnable jar in Docker image
  ).settings(
    name := "server",
    libraryDependencies ++= grpc,
    libraryDependencies ++= logging,
    libraryDependencies ++= Seq(
      tqa,
    ),

    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "native", xs @ _*) => MergeStrategy.first
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },

    mainClass in assembly := Some("net.hogerheijde.taxonomystore.server.TaxonomyStoreServer"),

    (maintainer in Docker) := "spamfilter@hogerheijde.net",
    dockerBaseImage := "openjdk:11-jre",
    dockerExposedPorts := Seq(50051),
    dockerLabels := Map("0.0.1" -> "0.0.1")
  )
