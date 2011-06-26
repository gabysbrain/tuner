package tuner

import tuner.gui.P5Panel

trait ColorMap {

  // Colors in processing are 0xAARRGGBB
  val colors:Array[Int]
  def breaks = (1 until colors.size-1).map {i => 
    i.toFloat*1f/(colors.size-1).toFloat
  } toArray

  // Linearly interpolates between lowColor and highColor
  def lerp(pct:Float) : Int = {
    val lb = breaks.segmentLength({v => v <= pct}, 0)
    val lowColor = colors(lb)
    val highColor = colors(lb+1)
    P5Panel.lerpColor(lowColor, highColor, pct, P5Panel.ColorSpace.HSB)
  }
}

object GreenColorMap extends ColorMap {
  val colors = Array(0xFFEDF8E9, 0xFF005A32)
}

object PurpleColorMap extends ColorMap {
  val colors = Array(0xFFF2F0F7, 0xFF4A1486)
}

object HotColorMap extends ColorMap {
  val colors = Array(0xFFE5E583, 0xFFE7901B, 0xFFF05E10, 
                     0xFFFA4A30, 0xFFA81010, 0xFF5C4747)
}

object OrangeColorMap extends ColorMap {
  val colors = Array(0xFFFEEDDE, 0xFF8C2D04)
}

object RedColorMap extends ColorMap {
  val colors = Array(0xFFFEE0D2, 0xFFDE2D26)
}

object BlueColorMap extends ColorMap {
  val colors = Array(0xFFEFF3FF, 0xFF08519C)
}

object GrayscaleColorMap extends ColorMap {
  val colors = Array(0xFF000000, 0xFFFFFFFF)
}

object ConstantBlackColorMap extends ColorMap {
  val colors = Array(0xFF000000, 0xFF000000)
}

object AlphaColorMap extends ColorMap {
  val colors = Array(0x00FFFFFF, 0xFFFFFFFF)
}

object CategoryColorMap {
  val COLORS = List(0x1B9E77, 0xD95F02, 0x7570B3, 0xE7298A, 
                    0x66A61E, 0xE6AB02, 0xA6761D, 0x666666).toArray

  def color(n:Int) : Int = {
    COLORS(n)
  }
}



