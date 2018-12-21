import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {
  lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val scalactic = "org.scalactic" %% "scalactic" % "3.0.3"

  lazy val tqa = "eu.cdevreeze.tqa" %% "tqa" % "0.8.9"

  lazy val logging = Seq(
    "ch.qos.logback" % "logback-core" % "1.2.3",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.slf4j" % "slf4j-api" % "1.7.25",
  )

  lazy val akka = Seq(
    "com.typesafe.akka" %% "akka-http" % "10.1.6",
    "com.typesafe.akka" %% "akka-http2-support" % "10.1.6",
    "com.typesafe.akka" %% "akka-stream" % "2.5.19",
  )


  val ioGrpcVersion = "1.17.1"
  lazy val grpc = Seq(
    "io.grpc" % "grpc-core" % ioGrpcVersion,
    "io.grpc" % "grpc-stub" % ioGrpcVersion,
    "io.grpc" % "grpc-netty" % ioGrpcVersion,
    "io.netty" % "netty-tcnative-boringssl-static" % "2.0.20.Final",
  )

  lazy val protobuf = Seq(
    "com.thesamet.scalapb" %% "compilerplugin" % "0.7.4",
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.7.4",
    "com.thesamet.scalapb" %% "scalapb-runtime" % "0.7.4" % "protobuf",
  )
}
