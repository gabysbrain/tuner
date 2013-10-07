package test.tuner.gp

import org.scalatest._
import org.scalatest.Matchers._

import test.tuner.Util._

import breeze.linalg._

import tuner.R
import tuner.gp.RGpBuilder

class GpModelSpec extends WordSpec {

  // building the R model directly in R
  def buildRGp(datafile:String, fields:List[String], resp:String) = {
    R.runCommand("source('%s')".format(tuner.Config.gpRScript))

    // Read the design file
    val rDesignFile = datafile.replace("\\", "/")
    R.runCommand("%s <- read.design('%s')".format(RGpBuilder.DESIGNRVAR, rDesignFile))
    // Create an r vector of all the strings
    val rvect = "c(" + fields.map("'"+_+"'").reduceLeft(_+","+_) + ")"
    //println("params: " + rvect)
    val fit = R.runCommand("%s <- fit.model(%s, %s, '%s')".
      format(RGpBuilder.MODELRVAR, RGpBuilder.DESIGNRVAR, rvect, resp))

    fit.asList
  }

  "A GP model" when {
    
    "built with R and a 3D data set" must {
      val dataset = resource("/datasets/3d.csv")
      val params = List("x1", "x2", "x3")
      val resp = "y1"
      val myGp = RGpBuilder.buildModel(dataset, params, resp, "sd")

      "have different predicted values when running a CV" in {
        val (myPreds, mySds) = myGp.crossValidate
        val myResps = myGp.responses

        // It is *very* unlikely these will be the same
        myPreds.length should equal (myResps.length)
        for(i <- 0 until myPreds.length) {
          myPreds(i) should not equal (myResps(i) +- 1e-6)
        }
      }

      "have some error at predicted values when running a CV" in {
        val (_, mySds) = myGp.crossValidate

        // It is *very* unlikely these will be the same
        all(mySds.toArray) should be > (0.0)
      }

      "have the same CV results" in {
        val (myPreds, mySds) = myGp.crossValidate
        val myResps = myGp.responses

        val rGp = buildRGp(dataset, params, resp)
        val rResps = DenseVector(rGp.at("Z").asDoubleArray)
        val rCV = new DenseMatrix(rResps.length, 2, rGp.at("cv").asDoubleArray)
        val (rPreds, rSds) = (rCV(::,0).toDenseVector, rCV(::,1).toDenseVector)

        myPreds.length should equal (rPreds.length)
        mySds.length should equal (rSds.length)
        for(i <- 0 until rPreds.length) {
          myPreds(i) should equal (rPreds(i) +- 1e-6)
          // The sds from the R model come back without being sqrted
          math.pow(mySds(i), 2) should equal (rSds(i) +- 1e-6)
        }
      }
    }
  }

}

