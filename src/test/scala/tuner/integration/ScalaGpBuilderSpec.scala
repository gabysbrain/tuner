package tuner.test.integration

import org.scalatest._
import org.scalatest.Matchers._

import tuner.gp.ScalaGpBuilder

import tuner.test.Util._

class ScalaGpBuilderSpec extends WordSpec with TryValues {

  "A ScalaGpBuilder class" when {
    "given a 3D test data set" must {
      val dataset = resource("/datasets/3d.csv")
      val params = List("x1", "x2", "x3")
      val resp = "y1"

      val gp = ScalaGpBuilder.buildModel(dataset, params, resp, "sd")

      "not have thrown an exception" in {
        gp should be a 'success
      }

      "return a valid model" in {
        gp.success.value.validateModel._1 should be (true)
      }

      "save a model that can be reloaded" in {
        val gpJson = gp.success.value.toJson
        val gp2 = tuner.gp.GpModel.fromJson(gpJson) 
        //gp2 should equal (gp)
      }
    }

    "given an 8D test data set" must {
      val dataset = resource("/datasets/8d.csv")
      val params = List("x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8")
      val resp = "y6"

      "return a valid model" in {
        val gp = ScalaGpBuilder.buildModel(dataset, params, resp, "sd")
        gp.success.value.validateModel._1 should be (true)
      }
    }

    "given a dataset that at one time produced sig2=0" must {
      val dataset = resource("/datasets/sig2_0.csv")
      val params = List("x1", "x2", "x3", "x4")
      val resp = "y"

      "return a valid model" in {
        val gp = ScalaGpBuilder.buildModel(dataset, params, resp, "sd")
        gp.success.value.mean should not be (0.0)
        gp.success.value.sig2 should be > 0.0
      }
    }
  }

}

