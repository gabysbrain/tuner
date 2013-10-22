package test.tuner

import org.scalatest._
import org.scalatest.Matchers._

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
        scriptOutput.size should be > (0)
      }
    }
    "run the sample script from the given directory" in {
      val scriptOutput = new java.io.ByteArrayOutputStream
      SampleRunner.runSamples(testSamples, resource("/sims/run_sim_noisy.sh", true), "/tmp", Some(scriptOutput))
      scriptOutput.toString should be ("/tmp")
    }
  }
}

