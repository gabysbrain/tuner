package tuner.geom

object Point {
  def apply(x:Float, y:Float) = new Point(x, y)
}

class Point(val x:Float, val y:Float) {
  
  override def toString = "(" + x + ", " + y + ")"
}

