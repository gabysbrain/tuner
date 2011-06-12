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

fork := true

javaOptions := {
  val jriPath = "/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri"
  Seq("-Djava.library.path=" + jriPath + ":/Users/tom/Projects/tuner/lib/opengl")
}

parallelExecution := false

mainClass := Some("tuner.Tuner")
  
