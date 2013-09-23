package tuner.util

import tuner.Color

object ColorLib {
  val scale = 0.7f

  def darker(c:Color) : Color = {
    val a = (c >> 24) & 0xFF
    val r = (c >> 16) & 0xFF
    val g = (c >> 8) & 0xFF
    val b = c & 0xFF

    Color(a << 24 | (scale*r).toInt << 16 | (scale*g).toInt << 8 | (scale*b).toInt)
  }
}

