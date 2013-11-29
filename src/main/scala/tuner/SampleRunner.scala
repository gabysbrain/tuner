package tuner

import scala.concurrent._
import ExecutionContext.Implicits.global

import java.io.File

import tuner.error.SamplingErrorException
import tuner.error.InvalidSamplingTableException

object SampleRunner {

  def runSamples(samples:Table, 
                 scriptPath:String, 
                 scriptDir:String, 
                 logStream:Option[java.io.OutputStream]) : Table = {
    if(samples.isEmpty) {
      new Table
    } else {
      val sampleFile = File.createTempFile("tuner_samples", ".csv")
      val designFile = File.createTempFile("tuner_design", ".csv")

      // We should validate the script path and dir somehow...
      val scriptArgs = scriptPath.split(" ").toList ++
                       List(sampleFile.getAbsolutePath, 
                            designFile.getAbsolutePath)
      val pb = new ProcessBuilder(scriptArgs:_*)
      pb.directory(new File(scriptDir))
      pb.redirectErrorStream(true)

      samples.toCsv(sampleFile.getAbsolutePath)

      val currentProc = pb.start
      // inputStream is the stdout and stderr of the proc
      logStream foreach {log => 
        future {readOutput(currentProc.getInputStream, log)}
      }
      currentProc.waitFor

      if(currentProc.exitValue != 0) {
        // this is an error
        throw new SamplingErrorException(currentProc.exitValue)
      } else {
        // We also need to validate the table that comes back from 
        // the sampling script
        val responseTable = Table.fromCsv(designFile.getAbsolutePath)
        if(samples.numRows != responseTable.numRows)
          throw new InvalidSamplingTableException(samples, responseTable)
        if(!samples.fieldNames.toSet.subsetOf(responseTable.fieldNames.toSet))
          throw new InvalidSamplingTableException(samples, responseTable)
        responseTable
      }
    }
  }

  private def readOutput(procStream:java.io.InputStream, 
                         logStream:java.io.OutputStream) : Unit = {
    // Too lazy to figure this out so this is from:
    // http://www.qualitybrain.com/?p=84
    val streamReader = new java.io.InputStreamReader(procStream)
    val bufferedReader = new java.io.BufferedReader(streamReader)
    val logger = new java.io.DataOutputStream(logStream)
    var line:String = null
    while({line = bufferedReader.readLine; line != null}){
      logger.writeChars(line)
    }
    bufferedReader.close
  }
}

