package tuner.test.project

import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers._

import java.io.File

import scala.actors.Actor

import tuner.test.util._

import tuner.Config
import tuner.DimRanges
import tuner.Progress
import tuner.Region
import tuner.Sampler
import tuner.project._

class RunningSamplesSpec extends FunSuite with BeforeAndAfterEach {

  // temp directory where the project config stuff is stored
  var projectPath:String = _

  override def beforeEach = {
    val resourcePath = getClass.getResource("/in_progress_orig").getPath
    val resDir = new File(resourcePath)

    val testDir = {
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
      if(f1.getName.endsWith(".sh")) {
        f2.setExecutable(true)
      }
    }
    projectPath = testDir.getAbsolutePath
  }

  override def afterEach = {
    val curDir = new java.io.File(projectPath)
    curDir.delete
  }

  test("test in progress running of samples") {
    val tmpProj = Project.fromFile(projectPath)

    tmpProj.isInstanceOf[RunningSamples] should be (true)

    val proj = tmpProj.asInstanceOf[RunningSamples]
    proj.start
    val Some(sr) = proj.sampleRunner

    // Make sure the inititial status is ok
    val Some(prog:Progress) = ListenerProxy.waitFor(proj, 5000) {
      case p:Progress => p
    }
    prog should have ('currentTime (0))
    prog should have ('totalTime (100))
    sr should have ('getState (Actor.State.Runnable))
    
    val Some(prog2:Progress) = ListenerProxy.waitFor(proj, 5000) {
      case p:Progress => p
    }

    prog2 should have ('currentTime (Config.samplingRowsPerReq))
    prog2 should have ('totalTime (100))
    prog2 should have ('ok (true))
  }

  test("test in progress progress query") {
    val random = new scala.util.Random
    val tmpProj = Project.fromFile(projectPath)

    tmpProj.isInstanceOf[RunningSamples] should be (true)

    val proj = tmpProj.asInstanceOf[RunningSamples]
    proj.start
    val Some(sr) = proj.sampleRunner


    // Make sure the inititial status is ok
    val Some(prog:Progress) = ListenerProxy.waitFor(proj, 5000) {
      case p:Progress => p
    }
    prog should have ('currentTime (Config.samplingRowsPerReq))
    prog should have ('totalTime (100))
    sr should have ('getState (Actor.State.Runnable))
    
    // Sleep for 5 seconds and then more samples should have finished
    val Some(prog2:Progress) = ListenerProxy.waitFor(proj, 5000) {
      case p:Progress => p
    }

    prog2 should have ('currentTime (Config.samplingRowsPerReq*2))
    prog2 should have ('totalTime (100))
    prog2 should have ('ok (true))

    // Sleep some more
    val Some(prog3:Progress) = ListenerProxy.waitFor(proj, 5000) {
      case p:Progress => p
    }

    prog3 should have ('currentTime (Config.samplingRowsPerReq*3))
    prog3 should have ('totalTime (100))
    prog3 should have ('ok (true))
  }


}

