package tuner

import java.lang.{Float => JFloat}

import java.io.DataInputStream
import java.io.BufferedInputStream
import java.io.FileInputStream

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage

import scala.io.Source

object ProbabilityField {
  
  def fromMhd(dirname:String, filename:String) : ProbabilityField = {
    val file = Source.fromFile(dirname + "/" + filename).getLines
    var xsize:Int = 0
    var ysize:Int = 0
    var fields:Int = 0
    var dataname:String = null
    file.foreach({line =>
      val splitLine = line.split("=").map(_.trim)
      if(splitLine(0).equals("DimSize")) {
        val sizes = splitLine(1).split(" ")
        ysize = sizes(0).toInt
        xsize = sizes(1).toInt
      } else if(splitLine(0).equals("ElementNumberOfChannels")) {
        fields = splitLine(1).toInt
      } else if(splitLine(0).equals("ElementDataFile")) {
        dataname = splitLine(1)
      }
    })

    val pf = new ProbabilityField(xsize, ysize, fields)
    //print("reading image " + dataname + "...")
    val in = new DataInputStream(
      new BufferedInputStream(new FileInputStream(dirname + "/" + dataname)))
      for(x <- 0 until xsize) {
    for(y <- 0 until ysize) {
        for(fnum <- 0 until fields) {
          //val flt = in.readFloat()
          val flt = readFloatLittleEndian(in)
          if(flt > 1.1) {
            throw new Exception("Probability over 1 (" + flt + ")")
          }
          if(flt < -1.1) {
            throw new Exception("Probability under 0 (" + flt + ")")
          }
          pf.data(fnum).set(y, x, flt)
        }
      }
    }
    //println("done")

    pf
  }

  def readFloatLittleEndian(in:DataInputStream) : Float = {
    var accum:Int = 0
    for(shift <- 0 until 32 by 8) {
      accum |= (in.readByte & 0xff) << shift
    }
    JFloat.intBitsToFloat(accum)
  }

  def readFloatBigEndian(in:DataInputStream) : Float = in.readFloat

}

class ProbabilityField(val xSize:Int, val ySize:Int, val numFields:Int) {
  
  val data = (0 until numFields).foldLeft(Nil:List[Grid2D]) {(lst:List[Grid2D], n:Int) => 
    val newMtx = new Grid2D((0 until ySize).toList.map({_.toFloat}),
                              (0 until xSize).toList.map({_.toFloat})) 
    newMtx :: lst
  }

  //val image = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB)
  var myImage:PImage = null

  def image(applet:PApplet) : PImage = {
    if(myImage == null) {
      val startTime = System.currentTimeMillis
      val startMax = (Float.MinValue, -1)
      myImage = applet.createImage(xSize, ySize, PConstants.RGB)
      myImage.loadPixels
        for(y <- 0 until ySize) {
      for(x <- 0 until xSize) {
          val (_, mx) = data.zipWithIndex.foldLeft(startMax) {(maxinfo, d) =>
            if(d._1.get(y,x) > maxinfo._1)
              (d._1.get(y,x), d._2)
            else
              maxinfo
          }
          myImage.pixels(y * xSize + x) = CategoryColorMap.color(mx)
        }
      }
      myImage.updatePixels
      val endTime = System.currentTimeMillis
      //println("Image time: " + (endTime - startTime) + "ms")
    }
    myImage
  }
}

