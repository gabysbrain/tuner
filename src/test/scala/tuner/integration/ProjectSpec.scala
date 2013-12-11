package tuner.test.integration

import org.scalatest._
import org.scalatest.Matchers._

import tuner.project.Project

import tuner.test.Util._

class ProjectSpec extends WordSpec {

  "A Project" when {
    "given a valid 8D project to load" must {
      val projPath = resource("/8d_viewable.proj")

      "load the project without errors" in {
        val proj = Project.fromFile(projPath)
      }
    }

    "given a valid 3D project to load" must {
      val projPath = resource("/3d_viewable.proj")

      "load the project without errors" in {
        val proj = Project.fromFile(projPath)
      }
    }
  }

}

