package tuner.test.unit

import tuner.project.Project
import tuner.project.Viewable

import tuner.test.Util._

class ViewableSpec extends UnitTest {

  val projPath = resource("/3d_viewable.proj")
  val proj = Project.fromFile(projPath)

  "When a Viewable is queried about the next stage with no samples" should {
      "return a viewable project" in {
      proj.next must haveClass[Viewable]
    }
  }
}

