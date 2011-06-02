package tuner.gui.util

import tuner.Config
import tuner.R

object AxisTicks {
  
  // Set up the labeling library
  R.runCommand("library(labeling)")

  def ticks(min:Float, max:Float, n:Int=Config.axisNumTicks) : List[Float] = {
    val cmd = "extended(%f, %f, %d)".format(min, max, n)
    val rTicks = R.runCommand(cmd)
    rTicks.asDoubles.toList.map {_.toFloat}
  }
}

