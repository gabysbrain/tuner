package tuner.test.functional

import org.scalatest.FeatureSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers._

import tuner.test.Util._

import tuner.Tuner
import tuner.project.Project

class SamplingSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfter {

  before {
    tuner.Config.testingMode = true
  }

  feature("Sampling should start when a project is opened") {
    scenario("Everything is ok") {
      Given("A properly-defined sampling project")
      val projPath = resource("/test_sampling_proj")
      val proj = Project.fromFile(projPath)
      proj shouldBe a [tuner.project.RunningSamples]
  
      When("The project is opened")
      Tuner.openProject(proj)
  
      Then("The sampling dialog box should open")
      // There's only 1 window in this test right now...
      val dlg = Tuner.openWindows.head
      dlg shouldBe a [tuner.gui.SamplingProgressBar]
      dlg.asInstanceOf[tuner.gui.SamplingProgressBar].
        console.scrollConsole.visible = true
  
      And("Sampling should start immediately")
      proj should be ('running)
  
      When("The sampling is done")
      // I'm not sure a spin-lock is good here
      while(proj.asInstanceOf[tuner.project.RunningSamples].running) {}
  
      Then("The dialog should close")
      dlg should not be ('visible)
    }
  }
}

