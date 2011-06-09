package tuner

import scala.actors.Actor
import scala.actors.Actor._

import java.io.File

class SampleRunner(project:Project) extends Actor {
  
  val sampleFile = File.createTempFile("tuner_samples", ".csv")
  val designFile = File.createTempFile("tuner_design", ".csv")

  val samples = project.newSamples

  var completedSamples:Int = 0

  val pb = new ProcessBuilder(project.scriptPath.get,
                              sampleFile.getAbsolutePath, 
                              designFile.getAbsolutePath)

  def unrunSamples = samples.numRows

  def totalSamples = completedSamples + unrunSamples

  def act = {
    while(samples.numRows > 0) {
      val subSamples = subsample
      subSamples.toCsv(sampleFile.getAbsolutePath)
      val proc = pb.start
      proc.waitFor // Run until we're done

      if(proc.exitValue != 0) {
        throw new Exception("Script exited with value " + proc.exitValue)
      }

      // Now the sampling is all done, load up the new points
      val newDesTbl = Table.fromCsv(designFile.getAbsolutePath)
      for(r <- 0 until newDesTbl.numRows) {
        val tpl = newDesTbl.tuple(r)
        val ds = project.designSites match {
          case Some(x) => x
          case None =>
            val tbl = new Table
            project.designSites = Some(tbl)
            tbl
        }
        ds.addRow(tpl.toList)
        samples.removeRow(0) // Always the first row
      }
      project.save(project.savePath)
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

