name := """play-authenticate-usage-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"
autoScalaLibrary := false
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.typesafe.slick" %% "slick-codegen" % "3.1.1",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "com.feth" %% "play-authenticate" % "0.8.1",
  "be.objectify"  %% "deadbolt-scala" % "2.5.0",
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.nappin" %% "play-recaptcha" % "2.1",
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "com.h2database" % "h2" % "1.4.193" % Test,
  specs2 % Test
)

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "secured-central" at "https://repo1.maven.org/maven2",
  Resolver.sonatypeRepo("releases")
)

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"