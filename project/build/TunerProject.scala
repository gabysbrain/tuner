import sbt._
import com.github.retronym.OneJarProject

class TunerProject(info : ProjectInfo) extends DefaultProject(info) with OneJarProject {

  val scalaToolsSnapshots = ScalaToolsSnapshots

  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.1"
  val scalaTest = "org.scalatest" % "scalatest_2.8.1" % "1.5"
  val lift_json = "net.liftweb" % "lift-json_2.8.0" % "2.1"
  val tablelayout = "tablelayout" % "TableLayout" % "20050920"
  val commonsMath = "org.apache.commons" % "commons-math" % "2.0"

  val jriPath = "/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri"
  val prefuse = "org.prefuse" % "prefuse" % "beta-20060220"

  override def compileOptions : Seq[CompileOption] = List(Deprecation, Unchecked)

  override def unmanagedClasspath : PathFinder = {
    super.unmanagedClasspath +++ 
      descendents(Path.fromFile(jriPath), "*.jar")
  }

  override def mainClass : Option[String] = Some("tuner.Tuner")
  
  // Need to fork to override the java.library.path
  override def fork = forkRun("-Djava.library.path=%s:/Users/tom/Projects/tuner/lib/opengl".format(jriPath)::Nil)

}

