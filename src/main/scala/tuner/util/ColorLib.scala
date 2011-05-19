package tuner.util

import processing.core.PApplet

object ColorLib {
  val scale = 0.7f

  def darker(c:Int) : Int = {
    val a = (c >> 24) & 0xFF
    val r = (c >> 16) & 0xFF
    val g = (c >> 8) & 0xFF
    val b = c & 0xFF

    a << 24 | (scale*r).toInt << 16 | (scale*g).toInt << 8 | (scale*b).toInt
  }
}

