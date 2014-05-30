import AssemblyKeys._

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

seq(assemblySettings: _*)

name := "IRCBalloon"

version := "0.8.1-fix2"

scalaVersion := "2.10.4"

fork in run := true

resolvers ++= Seq(
  "gettext-commons-site" at "http://gettext-commons.googlecode.com/svn/maven-repository"
)

libraryDependencies ++= Seq(
  "org.pircbotx" % "pircbotx" % "1.9",
  "com.typesafe.akka" %% "akka-actor" % "2.1.2",
  "org.xnap.commons" % "gettext-commons" % "0.9.6"
)

TaskKey[Unit]("xgettext") <<= (sources in Compile, name) map { (sources, name) =>
  <x>xgettext --from-code=utf-8 -L java -ktrc:1c,2 -ktrnc:1c,2,3 -ktr 
     -kmarktr -ktrn:1,2 -o po/{name}.pot {sources.mkString(" ")}</x> !
}

TaskKey[Unit]("msgfmt") <<= (classDirectory in Compile, name) map { (target, name) =>
  import java.io.File
  val poFiles = (PathFinder(new File("po")) ** "*.po").get
  <x>msgfmt --java2 -d {target} -r app.i18n.Messages {poFiles.mkString(" ")}</x> !
}

