package tuner.test.integration

import org.scalatest._
import org.scalatest.Matchers._

import tuner.test.Util._

import breeze.linalg._

import tuner.R
import tuner.Table
import tuner.gp.RGpBuilder

class RGpBuilderSpec extends WordSpec {

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
  
  "A GP model built with R" when {
    "built on the 3D dataset" must {
      val dataset = resource("/datasets/3d.csv")
      val params = List("x1", "x2", "x3")
      val resp = "y1"

      val myGp = RGpBuilder.buildModel(dataset, params, resp, "sd")
      val (myPreds, mySds) = myGp.crossValidate
      val myResps = myGp.responses

      val rGp = buildRGp(dataset, params, resp)
      
      "have the same first design row" in {
        val dataTbl = Table.fromCsv(dataset)
        val dataRow = DenseVector(params.map {f => dataTbl.tuple(0)(f).toDouble} toArray)
        //assertResult(dataRow) {myGp.design(0, ::).toDenseVector}
        val gpDesign = myGp.design(0, ::).toDenseVector
        gpDesign.length should equal (dataRow.length)
        for(i <- 0 until dataRow.length) {
          // This tolerance comes from the R translation
          gpDesign(i) should equal (dataRow(i).toDouble +- 1e-6)
        }
      }

      "have a 3 wide design matrix" in {
        assertResult(3) {myGp.design.cols}
      }

      "have the same correlation parameters as the R version" in {
        assert(myGp.thetas == DenseVector(rGp.at("beta").asDoubleArray))
      }


      "return the same predictions as the R model" in {
        val (myPred, myErr) = myGp.runSample(params.zip(List(0.5f, 0.5f, 0.5f)))
        val rData = R.runCommand("predict(%s, data.frame(x1=0.5, x2=0.5, x3=0.5))".format(RGpBuilder.MODELRVAR))
        myPred should equal (rData.asDouble +- 1e-6)
      }
    }
  }

}

