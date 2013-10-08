package tuner

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage

class PreviewImages(estModel:GpModel, imageDir:String, samples:Table) {
  
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
    //println("loading image " + count)
    fields(count).image(applet)
  }

  // Stuff to memoize last position
  /*
  var lastPoint:List[(String,Float)] = null
  var lastImage:PImage = null
  def image(applet:PApplet, pt:List[(String,Float)]) : PImage = {
    if(pt != lastPoint) {
      //val estFld = new ProbabilityField(applet, xSize, ySize, numFields)

      val newImg = applet.createImage(xSize, ySize, PConstants.RGB)
      newImg.loadPixels
      gpEstimators.zipWithIndex.foreach {tmp =>
        val (gps, i) = tmp
        val (x, y) = (i / xSize, i % xSize)
        val (_, mx) = gps.zipWithIndex.foldLeft((Double.MinValue, -1)) {(maxinfo, gpi) =>
          val (gp, i) = gpi
          val (estVal, _) = gp.runSample(pt)
          if(estVal > maxinfo._1)
            (estVal, i)
          else
            maxinfo
        }
        newImg.pixels(y * ySize + x) = CategoryColorMap.color(mx)
      }
      newImg.updatePixels

      lastImage = newImg
      lastPoint = pt
    }
    lastImage
  }
  */

  private def rebuildFields : List[List[GpModel]] = {

    print("building image predictors...")
    val gps = (0 until (xSize * ySize)).map({i =>
      val (x, y) = (i / xSize, i % xSize)
      (0 until numFields).map({fld =>
        val res = new org.jblas.DoubleMatrix(fields.map({pf => pf.data(fld).get(x, y).toDouble}))
        new GpModel(estModel.thetas, estModel.alphas, 
                    estModel.design, res, estModel.rInverse,
                    estModel.dims, estModel.respDim, estModel.errDim)
      }).toList
    }).toList
    println("done")
    gps
  }
}

