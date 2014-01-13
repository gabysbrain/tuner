package tuner.test.functional

import org.scalatest.FeatureSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers._

import tuner.test.Util._

import tuner.Tuner
import tuner.project.Project

class OpenProjectSpec extends FeatureSpec with GivenWhenThen {

  feature("Opening a viewable project shouldn't cause any errors") {
    Given("A project in a viewable state")
    val projPath = resource("/3d_viewable.proj")
    val proj = Project.fromFile(projPath)

    When("The project is opened")
    Tuner.openProject(proj)

    Then("No errors should occur")

    And("It should be viewable")
    proj shouldBe a [tuner.project.Viewable]
  }

}

