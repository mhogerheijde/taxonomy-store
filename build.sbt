import Dependencies._


ThisBuild / organization := "net.hogerheijde.taxonomy-provider"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.12.7"
ThisBuild / name         := "taxonomy-store"
ThisBuild / libraryDependencies ++= Seq(
  scalatest % Test,
  scalactic % Test,
)

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

lazy val api = project
  .in(file("api"))
  .settings(
    name := "api",
    libraryDependencies ++= protobuf,
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
  )


lazy val client = project
  .dependsOn(api, common)
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
  .enablePlugins(JavaAgent)
  .settings(
    name := "server",
    libraryDependencies ++= grpc,
    libraryDependencies ++= logging,
    libraryDependencies ++= Seq(
      tqa,
    ),
  )

