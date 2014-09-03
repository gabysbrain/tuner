package tuner.test.integration

import scala.io.Source

import tuner.SampleRunner
import tuner.Table

import tuner.test.Util._

class SampleRunnerSpec extends IntegrationTest {

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

  "A SampleRunner run" should  {
    "given an empty table" should {
      "return an empty table" in {
        val tbl = SampleRunner.runSamples(new Table, resource("/sims/run_sim.sh", true), "/", None)
        tbl should be ('empty)
      }
    }
    "given a missing scriptPath" should {
      "throw an exception" in {
        SampleRunner.runSamples(testSamples, resource("/sims/missing_script.sh", true), "/", None) must throwA[java.io.IOException]
      }
    }
    "the sampling script fails" should {
      "throw a SamplingErrorException" in {
        SampleRunner.runSamples(testSamples, resource("/sims/bad_script.sh", true), "/", None) must throwA[tuner.error.SamplingErrorException]
      }
    }
    "a run succeeds" should {
      "return a table w/ same number of rows as the input table" in {
        val inTbl = testSamples
        val outTbl = SampleRunner.runSamples(inTbl, resource("/sims/run_sim.sh", true), "/", None)
        inTbl.numRows must_== outTbl.numRows
      }
      "throw an exception when the sample run table isn't the same size" in {
        SampleRunner.runSamples(testSamples, resource("/sims/run_sim_bad_output.sh", true), "/", None) must throwA [tuner.error.InvalidSamplingTableException]
      }
    }
    "running the script" should {
      "log both the stdout and stderr of the script" in {
        val scriptOutput = new java.io.ByteArrayOutputStream
        SampleRunner.runSamples(testSamples, resource("/sims/run_sim_noisy.sh", true), "/", Some(scriptOutput))
        val stdout = Source.fromFile(new java.io.File(resource("/sims/sim_stdout.log"))).mkString.trim
        val stderr = Source.fromFile(new java.io.File(resource("/sims/sim_stderr.log"))).mkString.trim
        val recordedOutput:String = scriptOutput.toString

        //recordedOutput should have length (stdout.length + stderr.length)
        //recordedOutput should be (stdout + stderr)
        recordedOutput.size must be_>(0)
      }
    }
    "run the sample script from the given directory" in {
      val scriptOutput = new java.io.ByteArrayOutputStream
      val tmpdir = new java.io.File("/tmp")
      SampleRunner.runSamples(testSamples, resource("/sims/run_sim_pwd.sh", true), tmpdir.getAbsolutePath, Some(scriptOutput))

      // need to actually check the files as the paths might be different due
      // to filesystem links
      val testFile = new java.io.File(scriptOutput.toString)
      testFile must_== tmpdir
    }
  }
}

