package tuner.test

import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers._

import java.io.File

import scala.actors.Actor

import tuner.Config
import tuner.Project

class InProgressProjectSpec extends FunSuite with BeforeAndAfterEach {

  // temp directory where the project config stuff is stored
  var projectPath:String = _

  override def beforeEach = {
    val resourcePath = getClass.getResource("/in_progress_orig").getPath
    val resDir = new File(resourcePath)

    val testDir  = {
      val tmptmp = File.createTempFile("in_progress", System.nanoTime.toString)
      tmptmp.delete
      val tmp = new File(tmptmp.getAbsolutePath + ".d")
      tmp.mkdir
      tmp
    }

    // Copy all the files in the directory
    for(fileName <- resDir.list) {
      val f1 = new File(resDir.getAbsolutePath + "/" + fileName)
      val f2 = new File(testDir.getAbsolutePath + "/" + fileName)
      val in = new java.io.FileInputStream(f1)
      val out = new java.io.FileOutputStream(f2)
      val buf = Array.fill(1024)(0.toByte)
      var len = in.read(buf)
      while(len > 0) {
        out.write(buf, 0, len)
        len = in.read(buf)
      }
      in.close
      out.close
    }
    projectPath = testDir.getAbsolutePath
  }

  override def afterEach = {
    val curDir = new java.io.File(projectPath)
    curDir.delete
  }

  test("test in progress running of samples") {
    val proj = Project.fromFile(projectPath)
    val Some(sr) = proj.sampleRunner

    Thread.sleep(10)

    // Make sure the inititial status is ok
    proj.status should be (Project.RunningSamples(0, 100))
    sr.getState should be (Actor.State.Runnable)
    
    // Sleep for 5 seconds and then more samples should have finished
    Thread.sleep(5000)

    proj.status should be (Project.RunningSamples(Config.samplingRowsPerReq, 100))
  }

  test("test in progress progress query") (pending)

}

