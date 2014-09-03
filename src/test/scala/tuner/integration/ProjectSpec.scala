package tuner.test.integration

import tuner.project.Project

import scala.util.Try

import tuner.test.Util._

class ProjectSpec extends IntegrationTest {

  "A Project" should {
    "given a valid 8D project to load" should {
      val projPath = resource("/8d_viewable.proj")

      "load the project without errors" in {
        Try(Project.fromFile(projPath)) must beSuccessfulTry
      }
    }

    "given a valid 3D project to load" should {
      val projPath = resource("/3d_viewable.proj")

      "load the project without errors" in {
        Try(Project.fromFile(projPath)) must beSuccessfulTry
      }
    }
  }

}

