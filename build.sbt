// The simplest possible sbt build file is just one line:

scalaVersion := "2.11.12"
// That is, to create a valid sbt build, all you've got to do is define the
// version of Scala you'd like your project to use.

// ============================================================================

// Lines like the above defining `scalaVersion` are called "settings" Settings
// are key/value pairs. In the case of `scalaVersion`, the key is "scalaVersion"
// and the value is "2.12.4"

// It's possible to define many kinds of settings, such as:

name := "hello-world"
organization := "ch.epfl.scala"
version := "1.0-SNAPSHOT"
mainClass in (Compile, run) := Some("holtprog.Main")

libraryDependencies ++= Seq(
"org.typelevel" %% "cats-core" % "1.0.1",
"org.slf4j" % "slf4j-api" % "1.7.5",
 "org.slf4j" % "slf4j-simple" % "1.7.5",
"org.scalanlp" %% "breeze" % "0.13.2",
"org.scalanlp" %% "breeze-natives" % "0.13.2",
"org.scalaj" %% "scalaj-http" % "2.4.0",
  "org.scalanlp" %% "breeze-viz" % "0.13.2",
  "org.jsoup" % "jsoup" % "1.8.2",
"ch.qos.logback" % "logback-classic" % "1.2.3" % Test
)


lazy val scalaProgressBar = RootProject(uri(s"https://github.com/a8m/pb-scala.git"))
lazy val root = (project in file(".")).dependsOn(scalaProgressBar)
