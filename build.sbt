name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)
