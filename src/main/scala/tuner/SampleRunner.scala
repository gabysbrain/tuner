package tuner

import scala.actors.Actor
import scala.actors.Actor._

import java.io.File

import tuner.project.RunningSamples

class SampleRunner(project:RunningSamples) extends Actor {
  
  val sampleFile = File.createTempFile("tuner_samples", ".csv")
  val designFile = File.createTempFile("tuner_design", ".csv")

  val samples = project.newSamples

  var completedSamples:Int = 0

  var currentProcess:Process = null

  val pb = new ProcessBuilder(project.scriptPath,
                              sampleFile.getAbsolutePath, 
                              designFile.getAbsolutePath)
  pb.directory(new File(project.path))

  def stop = {
    if(currentProcess != null)
      currentProcess.destroy
  }

  def unrunSamples = samples.numRows

  def totalSamples = completedSamples + unrunSamples

  def act = {
    while(samples.numRows > 0) {
      val subSamples = subsample
      subSamples.toCsv(sampleFile.getAbsolutePath)
      currentProcess = pb.start
      currentProcess.waitFor // Run until we're done

      if(currentProcess.exitValue != 0) {
        throw new Exception("Script exited with value " + currentProcess.exitValue)
      }
      currentProcess = null

      // Now the sampling is all done, load up the new points
      val newDesTbl = Table.fromCsv(designFile.getAbsolutePath)
      for(r <- 0 until newDesTbl.numRows) {
        val tpl = newDesTbl.tuple(r)
        project.designSites.addRow(tpl.toList)
        samples.removeRow(0) // Always the first row
      }
      project.save
      completedSamples += newDesTbl.numRows

      // TODO: delete the file?
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

  private def subsample : Table = {
    val subSamples = new Table
    for(r <- 0 until math.min(samples.numRows, Config.samplingRowsPerReq)) {
      subSamples.addRow(samples.tuple(r).toList)
    }
    subSamples
  }

}

