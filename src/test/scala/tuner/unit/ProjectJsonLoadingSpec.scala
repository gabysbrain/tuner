package tuner.test.unit

import org.scalatest._
import org.scalatest.Matchers._

import tuner.project.{Project, ProjConfig}

import tuner.test.Util._

class ProjectJsonLoadingSpec extends WordSpec with PrivateMethodTester {
  val versionlessProjects = List("3d_viewable.proj", "8d_viewable.proj", 
                                 "no_images_proj", "has_images_proj")

  val loadJson = PrivateMethod[ProjConfig]('loadJson)

  "The project loader" must {
    "when given a project with no version number" when {
      "default to version 1" in {
        versionlessProjects.foreach {pn =>
          val path = resource(s"/${pn}")
          val proj = Project invokePrivate loadJson(path)
          proj.versionNumber should be (1)
        }
      }
    }
  }
}