
name := "Tuner"

version := "0.2"

scalaVersion := "2.9.1"

libraryDependencies += "org.bitbucket.gabysbrain" % "datescala_2.9.1" % "0.9"

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.9.1"

libraryDependencies += "net.liftweb" % "lift-json_2.9.1" % "2.4"

libraryDependencies += "tablelayout" % "TableLayout" % "20050920"

libraryDependencies += "org.apache.commons" % "commons-math" % "2.2"

libraryDependencies += "org.prefuse" % "prefuse" % "beta-20060220"

libraryDependencies += "org.japura" % "japura" % "1.15.1" from "http://downloads.sourceforge.net/project/japura/Japura/Japura%20v1.15.1/japura-1.15.1.jar"

scalacOptions := Seq("-deprecation", "-unchecked")

javacOptions := Seq("-Xlint:deprecation")

fork := true

javaOptions := {
  val openglPath = "lib/opengl/macosx"
  val jriPath = "/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri"
  Seq("-Djava.library.path=" + jriPath + ":" + openglPath)
  //val openglPath = """lib\opengl\windows64"""
  //val jriPath = """C:\Users\tom\Documents\R\win-library\2.13\rJava\jri"""
  //Seq("-Djava.library.path=" + jriPath + """\x64;""" + jriPath + ";" + openglPath)
}

parallelExecution := false

mainClass := Some("tuner.Tuner")


