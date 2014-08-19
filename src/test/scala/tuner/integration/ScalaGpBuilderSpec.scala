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

      /*
      "return a valid model" in {
        assert(gp.success.value.validateModel._1, "CV test failed")
      }
      */

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
        assert(gp.success.value.validateModel._1, "CV test failed")
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
    
    // This should be an easy function to fit.  It's just a bowl
    "given a 4D spherical function" must {
      val params = List("x1", "x2", "x3", "x4")
      val dims = new tuner.DimRanges(params.map(n => n -> (-1f, 1f)) toMap)
      val samples = {
        val inputs = tuner.Sampler.lhc(dims, 50)
        val outputs = new tuner.Table
        for(r <- 0 until inputs.numRows) {
          val row = inputs.tuple(r)
          val y = row.values.map(x => x*x).sum
          outputs.addRow((row + ("y" -> y)).toList)
        }
        outputs
      }
      val gp = ScalaGpBuilder.buildModel(samples, params, "y", "sd")

      "not have thrown an exception" in {
        gp should be a 'success
      }

      "return a valid model" in {
        assert(gp.success.value.validateModel._1, "CV test failed")
      }
    }
  }

}

