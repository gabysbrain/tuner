package tuner.test.project

import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers._

import java.io.File

import scala.actors.Actor

import tuner.Config
import tuner.DimRanges
import tuner.project._
import tuner.Region
import tuner.Sampler

class NewProjectSpec extends FunSuite {

  val newProjSavePath:String = getClass.getResource("/new_proj_path").getPath

  test("new project status") {
    val numSamples = 5
    val projName = "testing_project"
    val savePath = newProjSavePath + "/" + projName
    val scriptPath = "/"
    val inputs = List(("d1", 0f, 1f), 
                      ("d2", 0f, 1f), 
                      ("d3", 0f, 1f), 
                      ("d4", 0f, 1f))
    val np = new NewProject(projName, savePath, scriptPath, inputs)
    np.newSamples(numSamples, Sampler.random)

    np.newSamples should have ('numRows (5))
    np.newSamples should have ('fieldNames (inputs.map(_._1)))

    np.save(savePath)

    val np2 = Project.fromFile(savePath)

    np2.isInstanceOf[RunningSamples] should be (true)

    val sp = np2.asInstanceOf[RunningSamples]
    sp should have ('buildInBackground (false))
    sp should have ('sampleRunner (None))
    sp.newSamples.numRows should be (numSamples)
    sp should have ('totalTime (5))
    sp should have ('currentTime (0))
  }

  test("new project range should be full input range") {
    val dimRanges = List(("d1", 0f, 1f), 
                         ("d2", 0f, 1f), 
                         ("d3", 0f, 1f), 
                         ("d4", 0f, 1f))
    val numSamples = 5
    val projName = "testing_project"
    val savePath = newProjSavePath + "/" + projName
    val scriptPath = "/"
    val np = new NewProject(projName, savePath, scriptPath, dimRanges)

    // Make sure all the ranges equal each other
    dimRanges.foreach {case (fld, min, max) =>
      np.sampleRanges.range(fld) should be ((min, max))
    }
  }
}

