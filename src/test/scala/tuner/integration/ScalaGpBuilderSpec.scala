package tuner.test.integration

import tuner.gp.ScalaGpBuilder

import scala.util.Try

import tuner.test.Util._

class ScalaGpBuilderSpec extends IntegrationTest {

  "A ScalaGpBuilder class" should {
    "given a 3D test data set" should {
      val dataset = resource("/datasets/3d.csv")
      val params = List("x1", "x2", "x3")
      val resp = "y1"

      val gp = ScalaGpBuilder.buildModel(dataset, params, resp, "sd")

      "not have thrown an exception" in {
        gp must beSuccessfulTry
      }

      "have theta values close to what mlegp in R gives" in {
        gp must beSuccessfulTry.withValue {m:tuner.gp.GpModel =>
          val thetas = m.thetas
          thetas(0) must be ~(1.7 +/- 1e-1) 
        }
        gp must beSuccessfulTry.withValue {m:tuner.gp.GpModel =>
          val thetas = m.thetas
          thetas(1) must be ~(6.3 +/- 1e-1)
        }
        gp must beSuccessfulTry.withValue {m:tuner.gp.GpModel =>
          val thetas = m.thetas
          thetas(2) must be ~(34.4 +/- 1e-1)
        }
      }
      /*
      "return a valid model" in {
        assert(gp.success.value.validateModel._1, "CV test failed")
      }
      */

      "save a model that can be reloaded" in {
        gp must beSuccessfulTry.withValue {m:tuner.gp.GpModel =>
          val gpJson = m.toJson
          Try(tuner.gp.GpModel.fromJson(gpJson)) must beSuccessfulTry
        }
      }
    }

    "given an 8D test data set" should {
      val dataset = resource("/datasets/8d.csv")
      val params = List("x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8")
      val resp = "y6"

      "return a valid model" in {
        val gp = ScalaGpBuilder.buildModel(dataset, params, resp, "sd")
        gp must beSuccessfulTry.withValue {m:tuner.gp.GpModel =>
          m.validateModel._1 must beTrue
        }
      }
    }

    "given a dataset that at one time produced sig2=0" should {
      val dataset = resource("/datasets/sig2_0.csv")
      val params = List("x1", "x2", "x3", "x4")
      val resp = "y"

      "return a valid model" in {
        val gp = ScalaGpBuilder.buildModel(dataset, params, resp, "sd")
        gp must beSuccessfulTry.withValue {m:tuner.gp.GpModel =>
          m.mean must not be_== 0.0
          m.sig2 must be_>(0.0)
        }
      }
    }
    
    // This should be an easy function to fit.  It's just a bowl
    "given a 4D spherical function" should {
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
        gp must beSuccessfulTry
      }

      "return a valid model" in {
        gp must beSuccessfulTry.withValue {m:tuner.gp.GpModel =>
          m.validateModel._1 must beTrue
        }
      }
    }
  }

}

