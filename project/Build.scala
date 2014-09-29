
import sbt._
import Keys._

// Need to detect the OS
object OSDetector {
  sealed trait OSType
  case object Mac extends OSType
  case object Win extends OSType
  case object Nix extends OSType

  def os : OSType = {
    val osName = System.getProperty("os.name").toLowerCase
    if(osName.contains("mac"))      Mac
    else if(osName.contains("win")) Win
    else                            Nix // default to unix...
  }
}

object TunerBuild extends Build {

  import OSDetector._

  //val javaLibPathKey = SettingKey[Seq[String]]("java-library-path", "The full java library path we want to use")

  override lazy val settings = super.settings ++ Seq(
    resolvers := Seq()
  )

  lazy val root = Project(id="tuner",
                          base=file("."),
                          settings=Project.defaultSettings)
}
