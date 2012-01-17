package tuner

object Color {

  def apply(argb:Int) = {
    val a = (argb >> 24) & 0xFF
    val r = (argb >> 16) & 0xFF
    val g = (argb >> 8) & 0xFF
    val b = argb & 0xFF
    new Color(r/255f, g/255f, b/255f, a/255f)
  }
  def apply(r:Float, g:Float, b:Float, a:Float) = new Color(r, g, b, a)

  implicit def c2Int(c:Color) : Int = c.toInt
  implicit def c2Floats(c:Color) : (Float, Float, Float) = (c.r, c.g, c.b)
}

class Color(val r:Float, val g:Float, val b:Float, val a:Float) {

  def red = r
  def green = g
  def blue = b
  def alpha = a

}

