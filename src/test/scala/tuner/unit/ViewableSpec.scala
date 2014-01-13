package tuner.test.unit

import org.scalatest._
import org.scalatest.Matchers._

import tuner.project.Project
import tuner.project.Viewable

import tuner.test.Util._

class ViewableSpec extends WordSpec {

  "A Viewable project" when {
    val projPath = resource("/3d_viewable.proj")
    val proj = Project.fromFile(projPath)

    "queried about the next stage with no samples" must {
      "return a viewable project" in {
        proj.next shouldBe a [Viewable]
      }
    }
  }

}

