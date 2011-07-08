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

class ProjectSpec extends FunSuite {

  test("make sure projects in different stages are the right types") {
    val p1 = Project.fromFile(getClass.getResource("/build_gp_proj").getPath)
    p1.asInstanceOf[BuildingGp]

    val p2 = Project.fromFile(getClass.getResource("/in_progress_orig").getPath)
    p2.asInstanceOf[RunningSamples]

    val p3 = Project.fromFile(getClass.getResource("/has_images_proj").getPath)
    p3.asInstanceOf[Viewable]

    val p4 = Project.fromFile(getClass.getResource("/no_images_proj").getPath)
    p4.asInstanceOf[Viewable]
  }

}

