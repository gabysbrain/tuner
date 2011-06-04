package tuner

import scala.actors.Actor
import scala.actors.Actor._

import java.io.File

class SampleRunner(project:Project) extends Actor {
  
  val sampleFile = File.createTempFile("tuner_samples", "csv")
  val designFile = File.createTempFile("tuner_design", "csv")

  val samples = project.unrunSamples

  val pb = new ProcessBuilder(project.scriptPath.get,
                              sampleFile.getAbsolutePath, 
                              designFile.getAbsolutePath)

  def act = {
    loop {
    }
  }

  /** 
   * Splits a file into its directory and filename components
   */
  private def splitFile(fname:String) : (String,String) = {
    val f = new File(fname)
    (f.getParent, f.getName)
  }

  private def absPath(fname:String) : String = new File(fname).getAbsolutePath

}

