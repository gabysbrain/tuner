
// assembly task
import AssemblyKeys._

assemblySettings

//webstartSettings

name := "Tuner"

version := "0.2"

scalaVersion := "2.9.1"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

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

javaOptions <<= (jriPathKey, openglPathKey) map {(j,o) => Seq("-Djava.library.path=" + j + ":" + o, "-Xmx6G")}

// Set the classpath assets to the assembly jar
//classpathAssets <<= assembly map { jar:File => Seq(Asset(true, true, jar))}

fork := true

test in assembly := {}

parallelExecution := false

mainClass := Some("tuner.Tuner")

// testing stalls the build
test in assembly := {}

// Don't include the jogl stuff since that will come from jnlp
//excludedJars in assembly <<= (fullClasspath in assembly) map {cp =>
//  cp filter {List("jogl.all.jar", "gluegen-rt.jar") contains _.data.getName}
//}
//
//webstartGenConf := GenConf(
//  dname       = "CN=Thomas Torsney-Weir, OU=Developmetn, O=Simon Fraser University, L=Burnaby, ST=British Columbia, C=CA",
//  validity    = 365
//)
//
//webstartKeyConf := KeyConf(
//  keyStore    = file("keystore.jks"),
//  storePass   = "password",
//  alias       = "alias",
//  keyPass     = "password"
//)
//
//webstartJnlpConf := Seq(JnlpConf(
//  mainClass		  = "tuner.Tuner",
//  fileName        = "tuner.jnlp",
//  codeBase        = "http://cdn.bitbucket.org/gabysbrain/tuner/downloads",
//  title           = "Tuner",
//  vendor          = "TTW",
//  description     = "The Tuner Application",
//  iconName        = None,
//  splashName      = None,
//  offlineAllowed  = true,
//  allPermissions  = true,
//  j2seVersion     = "1.6+",
//  maxHeapSize     = 1024,
//  extensions      = List(ExtensionConf("jogl-all-awt", "http://jogamp.org/deployment/archive/rc/v2.0-rc10/jogl-all-awt.jnlp")),
//  archResources   = List(
//    ArchResource("Mac OS X", "x86_64", List(NativeLib("jri-natives-macosx.jar"))),
//    ArchResource("Mac OS X", "i386", List(NativeLib("jri-natives-macosx.jar"))))
//))

