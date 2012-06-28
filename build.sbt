import AssemblyKeys._

scalacOptions ++= Seq("-unchecked", "-deprecation")

seq(assemblySettings: _*)

name := "IRCBalloon"

version := "0.6"

scalaVersion := "2.9.1"

fork in run := true

resolvers ++= Seq(
    "gettext-commons-site" at "http://gettext-commons.googlecode.com/svn/maven-repository"
)

libraryDependencies ++= Seq(
    "org.pircbotx" % "pircbotx" % "1.7",
    "org.xnap.commons" % "gettext-commons" % "0.9.6"
)

TaskKey[Unit]("xgettext") := {
    val q = (sources in Compile)
    println("This is my action:" + q.map(_.toString))
}

