
import sbt._
import Keys._

import complete.DefaultParsers._

import com.izforge.izpack.compiler.CompilerConfig
    
//import org.clapper.sbtplugins.IzPackPlugin

object MyBuild extends Build {
 
  lazy val tuner = Project("tuner", file(".")).
    dependsOn(dateDotScala).
    settings(
      commands ++= Seq(installer)
    )
  lazy val dateDotScala = RootProject(file("date-scala"))

  def installer = Command.command("installer") {state =>
    //val installFile = "src" / "main" / "izpack" / "install.xml"
    val installFile = Path("src/main/izpack/install.xml")
    val installJar = Path("tuner" + "-" +
                     "0.9" + "-install.jar")
    IO.withTemporaryDirectory {baseDir =>
      val compilerConfig = new CompilerConfig(
        installFile.absolutePath,
        baseDir.getPath,
        CompilerConfig.STANDARD,
        installJar.absolutePath
      )
      //log.info("Creating installer in " + instalJar + " from " + installFile)
      compilerConfig.executeCompiler
    }
    state
  }

}

