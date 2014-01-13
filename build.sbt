
// assembly task
import AssemblyKeys._

assemblySettings

seq(Revolver.settings: _*)

name := "Tuner"

version := "0.2"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "org.bitbucket.gabysbrain" %% "datescala" % "0.9",
  "net.liftweb" %% "lift-json" % "2.5",
  "tablelayout" % "TableLayout" % "20050920",
  "org.prefuse" % "prefuse" % "beta-20060220",
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test",
  "org.scalatest" %% "scalatest" % "2.0.M8" % "test",
  "org.scalanlp" %% "breeze" % "0.5.2",
  "com.typesafe.akka" %% "akka-actor" % "2.2.1"
)

libraryDependencies += "org.japura" % "japura" % "1.15.1" from "http://downloads.sourceforge.net/project/japura/Japura/Japura%20v1.15.1/japura-1.15.1.jar"

libraryDependencies <<= (scalaVersion, libraryDependencies) {(sv, deps) =>
  deps :+ ("org.scala-lang" % "scala-swing" % sv)
}

scalacOptions := Seq("-deprecation", "-unchecked")

javacOptions := Seq("-Xlint:deprecation")

javaOptions := Seq("-Xmx6G", "-Dcom.github.fommil.netlib.BLAS=com.github.fommil.netlib.F2jBLAS", "-Dcom.github.fommil.netlib.LAPACK=com.github.fommil.netlib.F2jLAPACK", "-Dcom.github.fommil.netlib.ARPACK=com.github.fommil.netlib.F2jARPACK")


// Set the classpath assets to the assembly jar
//classpathAssets <<= assembly map { jar:File => Seq(Asset(true, true, jar))}

fork := true

parallelExecution := false

mainClass := Some("tuner.Tuner")

// testing stalls the assembly build
test in assembly := {}

// functional tests are really slow 
// plus they break the rest of the tests right now
testOptions in Test := Seq(Tests.Filter(s => !s.startsWith("tuner.test.functional")), Tests.Argument("-oDF"))

