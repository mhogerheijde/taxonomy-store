import sbt._

object Versions {
  val Scalatest = "3.0.5"
  val TQA = "0.8.9"
  val ScalaPB = "0.8.2"
  val ioGrpcVersion = "1.17.1"
}

object Dependencies {
  lazy val scalatest = Seq(
    "org.scalatest" %% "scalatest" % Versions.Scalatest,
    "org.scalactic" %% "scalactic" % Versions.Scalatest,
  )

  lazy val tqa = "eu.cdevreeze.tqa" %% "tqa" % Versions.TQA
  lazy val saxon = "net.sf.saxon" % "Saxon-HE" % "9.8.0-14"

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.3.2"

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

  lazy val grpc = Seq(
    "io.grpc" % "grpc-core" % Versions.ioGrpcVersion,
    "io.grpc" % "grpc-stub" % Versions.ioGrpcVersion,
    "io.grpc" % "grpc-netty" % Versions.ioGrpcVersion,
    "io.netty" % "netty-tcnative-boringssl-static" % "2.0.20.Final",
  )

  lazy val protobuf = Seq(
    "com.thesamet.scalapb" %% "compilerplugin" % Versions.ScalaPB,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % Versions.ScalaPB,
    "com.thesamet.scalapb" %% "scalapb-runtime" % Versions.ScalaPB % "protobuf",
  )
}
