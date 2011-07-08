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

class ViewableSpec extends FunSuite {

  test("make sure a project without images loads up correctly") {
    val projPath = getClass.getResource("/no_images_proj").getPath
    val proj = Project.fromFile(projPath).asInstanceOf[Viewable]
    proj should have ('previewImages (None))
  }

  test("make sure a project with images loads up correctly") {
    val projPath = getClass.getResource("/has_images_proj").getPath
    val proj = Project.fromFile(projPath).asInstanceOf[Viewable]
    proj.previewImages should be ('defined)
  }
}

