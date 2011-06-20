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

  def ticks(min:Float, max:Float, n:Int=Config.axisNumTicks) : List[Float] = {
    val cmd = "extended(%s, %s, %d)".format(min, max, n)
    val rTicks = R.runCommand(cmd)
    rTicks.asDoubles.toList.map {_.toFloat}
  }
}

