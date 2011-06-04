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
    var r = 0
    while(r < samples.numRows) {
      val subSamples = subsample(r)
      subSamples.toCsv(sampleFile.getAbsolutePath)
      val proc = pb.start
      proc.waitFor // Run until we're done

      r += subSamples.numRows
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

  private def subsample(startRow:Int) : Table = {
    val subSamples = new Table
    var row = startRow
    while(subSamples.numRows < Config.samplingRowsPerReq && 
          row < samples.numRows) {
      subSamples.addRow(samples.tuple(row).toList)
      row += 1
    }
    subSamples
  }

}

