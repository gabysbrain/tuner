
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

libraryDependencies += "org.jblas" % "jblas" % "1.2.3"

libraryDependencies += "org.japura" % "japura" % "1.15.1" from "http://downloads.sourceforge.net/project/japura/Japura/Japura%20v1.15.1/japura-1.15.1.jar"

scalacOptions := Seq("-deprecation", "-unchecked")

javacOptions := Seq("-Xlint:deprecation")

javaOptions <<= (jriPathKey, openglPathKey) map {(j,o) => Seq("-Djava.library.path=" + j + ":" + o, "-Xmx6G")}

// Set the classpath assets to the assembly jar
//classpathAssets <<= assembly map { jar:File => Seq(Asset(true, true, jar))}

fork := true

test in assembly := {}

parallelExecution := false

mainClass := Some("tuner.Tuner")

// testing stalls the build
test in assembly := {}

