package test.tuner.gp

import org.scalatest._

import tuner.gp.ScalaGpBuilder

import test.tuner.Util._

class ScalaGpBuilderSpec extends WordSpec {

  "A ScalaGpBuilder class" when {
    "given a 3D test data set" should {
      "return a valid model" in {
        val gp = ScalaGpBuilder.buildModel(resource("/datasets/3d.csv"), 
                                           List("x1", "x2", "x3"), 
                                           "y1", "sd")
        assert(gp.validateModel._1)
      }
    }

    "given an 8D test data set" should {
      "return a valid model" in {
        val gp = ScalaGpBuilder.buildModel(
          resource("/datasets/8d.csv"), 
          List("x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8"), 
          "y6", "sd")
        assert(gp.validateModel._1)
      }
    }
  }

}

