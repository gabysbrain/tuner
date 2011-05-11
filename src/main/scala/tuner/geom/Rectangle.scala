package tuner.geom

object Rectange {
  
  def apply(topLeft:Point, bottomRight:Point) : Rectangle = 
    new Rectangle(topLeft, bottomRight)
  def apply(topLeft:(Float,Float), bottomRight:(Float,Float)) : Rectangle =
    apply(Point(topLeft._1, topLeft._2), Point(bottomRight._1, bottomRight._2))
}

class Rectangle(val topLeft:Point, val bottomRight:Point) {

  def width = math.abs(bottomRight.x - topLeft.x)
  def height = math.abs(bottomRight.y - topLeft.y)
}

