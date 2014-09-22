package tuner

import breeze.linalg.{DenseMatrix, DenseVector}

import tuner.gp.GpModel

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage

import com.typesafe.scalalogging.slf4j.LazyLogging

class PreviewImages(estModel:GpModel, imageDir:String, samples:Table)
    extends LazyLogging {

  val FIELDFILEFORMAT = "dat-prop%03d.mhd"

  val fields = new Array[ProbabilityField](samples.numRows)
  // Load in all the images
  for(r <- 0 until samples.numRows) {
    val count = r + 1
    val imgFilename = FIELDFILEFORMAT.format(count)
    fields(r) = ProbabilityField.fromMhd(imageDir, imgFilename)
  }

  // Create a gp model for each pixel in each field
  //val gpEstimators:List[List[GpModel]] = rebuildFields

  def xSize : Int = fields(0).xSize

  def ySize : Int = fields(0).ySize

  def numFields : Int = fields(0).numFields

  def image(applet:PApplet, count:Int) : PImage = {
    logger.info("loading image " + count)
    fields(count).image(applet)
  }

  private def rebuildFields : List[List[GpModel]] = {

    logger.info("building image predictors...")
    val gps = (0 until (xSize * ySize)).map({i =>
      val (x, y) = (i / xSize, i % xSize)
      (0 until numFields).map({fld =>
        val res = new DenseVector(fields.map({pf => pf.data(fld).get(x, y).toDouble}))
        new GpModel(estModel.thetas, estModel.alphas,
                    estModel.design, res, estModel.rInverse,
                    estModel.dims, estModel.respDim, estModel.errDim)
      }).toList
    }).toList
    logger.info("done")
    gps
  }
}
