
// assembly task
import AssemblyKeys._

assemblySettings

seq(Revolver.settings: _*)

name := "Tuner"

version := "0.2"

scalaVersion := "2.9.1"

libraryDependencies += "org.bitbucket.gabysbrain" %% "datescala" % "0.9"

libraryDependencies <<= (scalaVersion, libraryDependencies) {(sv, deps) =>
  deps :+ ("org.scala-lang" % "scala-swing" % sv)
}

libraryDependencies += "net.liftweb" %% "lift-json" % "2.4"

libraryDependencies += "tablelayout" % "TableLayout" % "20050920"

libraryDependencies += "org.apache.commons" % "commons-math" % "2.2"

libraryDependencies += "org.prefuse" % "prefuse" % "beta-20060220"

libraryDependencies += "org.japura" % "japura" % "1.15.1" from "http://downloads.sourceforge.net/project/japura/Japura/Japura%20v1.15.1/japura-1.15.1.jar"

scalacOptions := Seq("-deprecation", "-unchecked")

javacOptions := Seq("-Xlint:deprecation")

fork := true

test in assembly := {}

javaOptions := {
  val openglPath = "lib/opengl/macosx"
  val jriPath = "/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri"
  //Seq("-Djava.library.path=" + jriPath + ":" + openglPath)
  Seq("-Djava.library.path=" + jriPath)
  //val openglPath = """lib\opengl\windows64"""
  //val jriPath = """C:\Users\tom\Documents\R\win-library\2.13\rJava\jri"""
  //Seq("-Djava.library.path=" + jriPath + """\x64;""" + jriPath)
}

parallelExecution := false

mainClass := Some("tuner.Tuner")

// testing stalls the build
test in assembly := {}

// Don't include the jogl stuff since that will come from jnlp
excludedJars in assembly <<= (fullClasspath in assembly) map {cp =>
  cp filter {List("jogl.all.jar", "gluegen-rt.jar") contains _.data.getName}
}

