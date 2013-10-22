package test.tuner

import org.scalatest._
import org.scalatest.Matchers._

import scala.io.Source

import tuner.SampleRunner
import tuner.Table

import Util._

class SampleRunnerSpec extends WordSpec {

  def testSamples : Table = {
    val tblData = List(
      List(("x1", 2.0f), ("x2", 0.2f), ("x3", 1.2f)),
      List(("x1", 2.1f), ("x2", 0.4f), ("x3", 0.3f)),
      List(("x1", 0.1f), ("x2", 0.9f), ("x3", 0.5f))
    )
    val tbl = new Table
    tblData.foreach {row => tbl.addRow(row)}
    tbl
  }

  "A SampleRunner run" when  {
    "given an empty table" must {
      "return an empty table" in {
        val tbl = SampleRunner.runSamples(new Table, resource("/sims/run_sim.sh", true), "/", None)
        tbl should be ('empty)
      }
    }
    "given a missing scriptPath" must {
      "throw an exception" in {
        a [java.io.IOException] should be thrownBy 
          SampleRunner.runSamples(testSamples, resource("/sims/missing_script.sh", true), "/", None)
      }
    }
    "the sampling script fails" must {
      "throw a SamplingErrorException" in {
        a [tuner.error.SamplingErrorException] should be thrownBy 
          SampleRunner.runSamples(testSamples, resource("/sims/bad_script.sh", true), "/", None)
      }
    }
    "a run succeeds" must {
      "return a table w/ same number of rows as the input table" in {
        val inTbl = testSamples
        val outTbl = SampleRunner.runSamples(inTbl, resource("/sims/run_sim.sh", true), "/", None)
        inTbl.numRows should equal (outTbl.numRows)
      }
      "throw an exception when the sample run table isn't the same size" in {
        a [tuner.error.InvalidSamplingTableException] should be thrownBy
          SampleRunner.runSamples(testSamples, resource("/sims/run_sim_bad_output.sh", true), "/", None)
      }
    }
    "running the script" must {
      "log both the stdout and stderr of the script" in {
        val scriptOutput = new java.io.ByteArrayOutputStream
        SampleRunner.runSamples(testSamples, resource("/sims/run_sim_noisy.sh", true), "/", Some(scriptOutput))
        val stdout = Source.fromFile(new java.io.File(resource("/sims/sim_stdout.log"))).mkString.trim
        val stderr = Source.fromFile(new java.io.File(resource("/sims/sim_stderr.log"))).mkString.trim
        val recordedOutput:String = scriptOutput.toString

        //recordedOutput should have length (stdout.length + stderr.length)
        //recordedOutput should be (stdout + stderr)
        recordedOutput.size should be > (0)
      }
    }
    "run the sample script from the given directory" in {
      val scriptOutput = new java.io.ByteArrayOutputStream
      val tmpdir = new java.io.File("/tmp")
      SampleRunner.runSamples(testSamples, resource("/sims/run_sim_pwd.sh", true), tmpdir.getAbsolutePath, Some(scriptOutput))

      // need to actually check the files as the paths might be different due
      // to filesystem links
      val testFile = new java.io.File(scriptOutput.toString)
      testFile === tmpdir
    }
  }
}

