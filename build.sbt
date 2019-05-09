name := """play-authenticate-usage-scala"""

version := "1.2.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"
crossScalaVersions := Seq("2.11.12", "2.12.6")

autoScalaLibrary := false
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

// needed for adrianhurt's play-bootstrap dependency
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.3",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.3",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "com.typesafe.slick" %% "slick-codegen" % "3.2.1",
  "org.postgresql" % "postgresql" % "9.4.1212",
  "com.feth" %% "play-authenticate" % "0.9.1-SNAPSHOT",
  "be.objectify"  %% "deadbolt-scala" % "2.6.1",
  "org.webjars" %% "webjars-play" % "2.6.3",
  "org.webjars" % "bootstrap" % "3.3.7-1" exclude("org.webjars", "jquery"),
  "org.webjars" % "jquery" % "3.2.1",
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.nappin" %% "play-recaptcha" % "2.3",
  "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B3",
  "org.webjars" % "font-awesome" % "4.7.0",
  "org.webjars" % "bootstrap-datepicker" % "1.4.0",
  "com.warrenstrange" % "googleauth" % "1.1.2",
  cacheApi,
  ehcache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  "com.h2database" % "h2" % "1.4.193" % Test,
  specs2 % Test
)

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "secured-central" at "https://repo1.maven.org/maven2",
  Resolver.sonatypeRepo("releases")
)

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"