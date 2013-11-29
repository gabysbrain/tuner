package tuner.test.integration

import org.scalatest._

import tuner.gp.ScalaGpBuilder

import tuner.test.Util._

class ScalaGpBuilderSpec extends WordSpec {

  "A ScalaGpBuilder class" when {
    "given a 3D test data set" should {
      val dataset = resource("/datasets/3d.csv")
      val params = List("x1", "x2", "x3")
      val resp = "y1"

      "return a valid model" in {
        val gp = ScalaGpBuilder.buildModel(dataset, params, resp, "sd")
        assert(gp.validateModel._1)
      }
    }

    "given an 8D test data set" should {
      val dataset = resource("/datasets/8d.csv")
      val params = List("x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8")
      val resp = "y6"

      "return a valid model" in {
        val gp = ScalaGpBuilder.buildModel(dataset, params, resp, "sd")
        assert(gp.validateModel._1)
      }
    }
  }

}

