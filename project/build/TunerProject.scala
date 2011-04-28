import sbt._

class TunerProject(info : ProjectInfo) extends DefaultProject(info) {

  val scalaToolsSnapshots = ScalaToolsSnapshots
  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.1"

  override def compileOptions : Seq[CompileOption] = List(Deprecation)

  override def mainClass : Option[String] = Some("Tuner")
  
}

