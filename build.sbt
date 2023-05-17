ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0-RC6"

lazy val root = (project in file("."))
  .aggregate(simulation)
  .settings(
    name := "radar-data-sim",
    organization := "com.terrycode.radar"
  )

lazy val simulation = (project in file ("simulation"))
  .settings(
    name := "simulation",
    libraryDependencies ++= deps
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)

val fs2Version = "3.7.0"
val http4sVersion = "0.23.18"
val tapirVersion  = "1.3.0"

lazy val deps = Seq(
  "org.typelevel" %% "cats-effect" % "3.5.0",

  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version,
  "com.github.fd4s" %% "fs2-kafka" % "3.0.1",

  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,

  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,

  "com.softwaremill.sttp.tapir" %% "tapir-jsoniter-scala" % "1.2.12",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.23.0",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.23.0" % "provided",
  "com.github.plokhotnyuk.rtree2d" %% "rtree2d-core" % "0.11.12",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.4.7",

  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
  "com.softwaremill.sttp.client3" %% "circe" % "3.8.13" % Test,
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
)