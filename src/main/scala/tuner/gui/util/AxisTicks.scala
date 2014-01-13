package tuner.gui.util

import tuner.Config

import org.sittingbull.gt.util.XWilkinson
import org.sittingbull.gt.util.NiceStepSizeGenerator

import scala.collection.JavaConverters._

object AxisTicks {
  
  // Set up the labeling library
  val labeler = new XWilkinson(new NiceStepSizeGenerator)
  labeler.setLooseFlag(true)

  def numTicks(width:Float, fontSize:Float) : Int = {
    val labelSpace = 3
    math.min(
      Config.axisNumTicks,
      math.floor((width - labelSpace) / (fontSize + labelSpace)).toInt - 1
    )
  }

  def ticks(min:Float, max:Float, width:Float, fontSize:Float) : List[Float] = 
    ticks(min, max, numTicks(width, fontSize))

  def ticks(min:Float, max:Float, n:Int=Config.axisNumTicks) : List[Float] = {
    if(min == max) {
      throw new Exception("min cannot equal max")
    }
    if(min.isNaN) {
      throw new Exception("min cannot be NaN")
    }
    if(max.isNaN) {
      throw new Exception("max cannot be NaN")
    }
    if(n < 2) {
      Nil
    } else {
      //println(s"ticks for ${min} ${max} ${n}")
      val labelInfo = labeler.search(min, max, n)
      labelInfo.toList.asScala.map {_.toFloat} toList
    }
  }

  def ticksAndRange(min:Float, max:Float, width:Float, fontSize:Float) 
      : (List[Float], (Float,Float)) = {
    
    val myTicks = ticks(min, max, width, fontSize)
    val range = if(myTicks.isEmpty) {
      (min, max)
    } else {
      (myTicks.min, myTicks.max)
    }
    (myTicks, range)
  }
}

