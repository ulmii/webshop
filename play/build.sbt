name := """play"""
organization := "uj"

version := "1.0-SNAPSHOT"
resolvers += Resolver.jcenterRepo

lazy val play = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.12.10"

routesGenerator := InjectedRoutesGenerator

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= Seq(ehcache, ws, specs2 % Test, guice, filters)

libraryDependencies ++= Seq(
  // Enable reactive mongo for Play 2.8
  "org.reactivemongo" %% "play2-reactivemongo" % "0.20.13-play28",
  // Provide JSON serialization for reactive mongo
  "org.reactivemongo" %% "reactivemongo-play-json-compat" % "1.0.1-play28",
  // Provide BSON serialization for reactive mongo
  "org.reactivemongo" %% "reactivemongo-bson-compat" % "0.20.13",
  // Provide JSON serialization for Joda-Time
  "com.typesafe.play" %% "play-json-joda" % "2.7.4",

  "com.mohiva" %% "play-silhouette" % "5.0.7",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.7",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.7",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.7",
  "com.mohiva" %% "play-silhouette-persistence-reactivemongo" % "5.0.6",
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.iheart" %% "ficus" % "1.5.0",
  "org.apache.commons" % "commons-lang3" % "3.12.0"
)
