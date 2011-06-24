name := "Tuner"

version := "0.9"

scalaVersion := "2.8.1"

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.8.1"

libraryDependencies += "net.liftweb" % "lift-json_2.8.0" % "2.1"

libraryDependencies += "tablelayout" % "TableLayout" % "20050920"

libraryDependencies += "org.apache.commons" % "commons-math" % "2.0"

libraryDependencies += "org.prefuse" % "prefuse" % "beta-20060220"

libraryDependencies += "org.scalatest" % "scalatest_2.8.1" % "1.5" % "test"

scalacOptions := Seq("-deprecation", "-unchecked")

javacOptions := Seq("-Xlint:deprecation")

fork := true

javaOptions := {
  val openglPath = "lib/opengl/macosx"
  val jriPath = "/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri"
  Seq("-Djava.library.path=" + jriPath + ":" + openglPath)
  //val jriPath = """C:\Users\tom\Documents\R\win-library\2.13\rJava\jri"""
  //Seq("-Djava.library.path=" + jriPath + """\x64;""" + jriPath)
}

parallelExecution := false

mainClass := Some("tuner.Tuner")


