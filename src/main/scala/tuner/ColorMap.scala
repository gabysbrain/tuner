package tuner

import tuner.gui.P5Panel

trait ColorMap {

  // Colors in processing are 0xAARRGGBB
  val colors:Array[Color]
  def breaks = (1 until colors.size-1).map {i => 
    i.toFloat*1f/(colors.size-1).toFloat
  } toArray

  // Linearly interpolates between lowColor and highColor
  def lerp(pct:Float) : Color = {
    val lb = breaks.segmentLength({v => v <= pct}, 0)
    val lowColor = colors(lb)
    val highColor = colors(lb+1)
    Color(P5Panel.lerpColor(lowColor, highColor, pct, P5Panel.ColorSpace.HSB))
  }
}

object GreenColorMap extends ColorMap {
  val colors = Array(Color(0xFFEDF8E9), Color(0xFF005A32))
}

object PurpleColorMap extends ColorMap {
  val colors = Array(Color(0xFFF2F0F7), Color(0xFF4A1486))
}

object HotColorMap extends ColorMap {
  val colors = Array(Color(0xFFE5E583), Color(0xFFE7901B), Color(0xFFF05E10), 
                     Color(0xFFFA4A30), Color(0xFFA81010), Color(0xFF5C4747))
}

object OrangeColorMap extends ColorMap {
  val colors = Array(Color(0xFFFEEDDE), Color(0xFF8C2D04))
}

object RedColorMap extends ColorMap {
  val colors = Array(Color(0xFFFEE0D2), Color(0xFFDE2D26))
}

object BlueColorMap extends ColorMap {
  val colors = Array(Color(0xFFEFF3FF), Color(0xFF08519C))
}

object GrayscaleColorMap extends ColorMap {
  val colors = Array(Color(0xFF000000), Color(0xFFFFFFFF))
}

object ConstantBlackColorMap extends ColorMap {
  val colors = Array(Color(0xFF000000), Color(0xFF000000))
}

object AlphaColorMap extends ColorMap {
  val colors = Array(Color(0x00FFFFFF), Color(0xFFFFFFFF))
}

object CategoryColorMap {
  val COLORS = Array(Color(0x1B9E77), Color(0xD95F02), Color(0x7570B3), 
                     Color(0xE7298A), Color(0x66A61E), Color(0xE6AB02), 
                     Color(0xA6761D), Color(0x666666))

  def color(n:Int) : Color = {
    COLORS(n)
  }
}



