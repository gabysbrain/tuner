
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

  val jriPathKey = SettingKey[String]("jri-path", "Path to the JRI libary.  Used for setting java.library.path")
  val openglPathKey = SettingKey[String]("opengl-path", "Path to the OpenGL libary.  Used for setting java.library.path")
  val javaLibPathKey = SettingKey[Seq[String]]("java-library-path", "The full java library path we want to use")

  override lazy val settings = super.settings ++ Seq(
    jriPathKey := jriLibPath(os),
    openglPathKey := openglLibPath(os),
    javaLibPathKey := Seq(jriLibPath(os), openglLibPath(os)),
    resolvers := Seq()
  )

  lazy val root = Project(id="tuner", 
                          base=file("."), 
                          settings=Project.defaultSettings)

  def jriLibPath(theOS:OSType):String = theOS match {
    case OSDetector.Mac => "/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri"
    case OSDetector.Win => ""
    case OSDetector.Nix => ""
  }

  def openglLibPath(theOS:OSType):String = theOS match {
    case OSDetector.Mac => "lib/opengl/macosx"
    case OSDetector.Win => "lib\\opengl\\windows" + 
                           System.getProperty("sun.arch.data.model")
    case OSDetector.Nix => "lib/opengl/linux" +
                           System.getProperty("sun.arch.data.model")
  }
}

