import AssemblyKeys._

scalacOptions ++= Seq("-unchecked", "-deprecation")

seq(assemblySettings: _*)

name := "IRCBalloon"

version := "0.4"

scalaVersion := "2.9.1"

fork in run := true
