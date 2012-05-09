import AssemblyKeys._

scalacOptions ++= Seq("-unchecked", "-deprecation")

seq(assemblySettings: _*)

name := "IRCBalloon"

version := "0.1"

scalaVersion := "2.9.1"

fork in run := true
