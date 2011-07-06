package tuner

import scala.actors.Actor
import scala.actors.Actor._

import java.io.File

import tuner.project.RunningSamples

class SampleRunner(project:RunningSamples) extends Actor {
  
  val sampleFile = File.createTempFile("tuner_samples", ".csv")
  val designFile = File.createTempFile("tuner_design", ".csv")

  val samples = project.newSamples

  private var currentProcess:Process = null

  val cmd = project.scriptPath + " " + 
            sampleFile.getAbsolutePath + " " + 
            designFile.getAbsolutePath
  val pb = new ProcessBuilder(project.scriptPath,
                              sampleFile.getAbsolutePath, 
                              designFile.getAbsolutePath)
  pb.directory(new File(project.path))
  pb.redirectErrorStream(true)

  def stop = {
    if(currentProcess != null)
      currentProcess.destroy
  }

  def act = {
    var error = false
    while(samples.numRows > 0 && !error) {
      val subSamples = subsample
      project ! ConsoleLine("> " + cmd)
      subSamples.toCsv(sampleFile.getAbsolutePath)
      currentProcess = pb.start
      readOutput(currentProcess.getInputStream)
      readOutput(currentProcess.getErrorStream)
      currentProcess.waitFor // Run until we're done

      if(currentProcess.exitValue != 0) {
        error = true
        project ! SamplingError(currentProcess.exitValue)
      } else {
        currentProcess = null

        // Now the sampling is all done, load up the new points
        val newDesTbl = Table.fromCsv(designFile.getAbsolutePath)
        for(r <- 0 until newDesTbl.numRows) {
          val tpl = newDesTbl.tuple(r)
          project.designSites.addRow(tpl.toList)
          samples.removeRow(0) // Always the first row
        }
        project.save()
        project ! SamplesCompleted(newDesTbl.numRows)

        // TODO: delete the file?
      }
    }
    // We're only done if there was no error
    if(!error) {
      project ! SamplingComplete
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

  private def readOutput(stream:java.io.InputStream) : Unit = {
    actor {
      // Too lazy to figure this out so this is from:
      // http://www.qualitybrain.com/?p=84
      val streamReader = new java.io.InputStreamReader(stream)
      val bufferedReader = new java.io.BufferedReader(streamReader)
      var line:String = null
      while({line = bufferedReader.readLine; line != null}){
        println("here " + line)
        project ! ConsoleLine(line)
      }
      bufferedReader.close
      println("done reading")
    }
  }

}

