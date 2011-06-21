package tuner.gui.util

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
      val cmd = "extended(%s, %s, %d)".format(min, max, n)
      val rTicks = R.runCommand(cmd)
      rTicks.asDoubles.toList.map {_.toFloat}
    }
  }
}

