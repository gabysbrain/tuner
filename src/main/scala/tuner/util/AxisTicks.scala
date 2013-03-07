package tuner.util

import tuner.Config
import tuner.R

object AxisTicks {
  
  // Set up the labeling library
  try {
    R.runCommand("library(labeling)")
  } catch {
    case e:Exception =>
      throw new Exception("error loading labeling R libary")
  }

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
    if(n < 2) {
      Nil
    } else {
      val cmd = "extended(%s, %s, %d, only.loose=TRUE)".format(min, max, n)
      val rTicks = R.runCommand(cmd)
      rTicks.asDoubleArray.toList.map {_.toFloat}
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

