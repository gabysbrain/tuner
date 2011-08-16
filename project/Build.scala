
import sbt._

object MyBuild extends Build {
 
  lazy val tuner = Project("tuner", file(".")) dependsOn(dateDotScala)
  lazy val dateDotScala = RootProject(file("date-scala"))

}

