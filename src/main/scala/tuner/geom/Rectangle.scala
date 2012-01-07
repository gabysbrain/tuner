package tuner.geom

object Rectangle {
  
  def apply(topLeft:Point, bottomRight:Point) : Rectangle = 
    new Rectangle(topLeft, bottomRight)
  def apply(topLeft:(Float,Float), bottomRight:(Float,Float)) : Rectangle =
    apply(Point(topLeft._1, topLeft._2), Point(bottomRight._1, bottomRight._2))
  def apply(topLeft:(Float,Float), width:Float, height:Float) : Rectangle =
    apply(Point(topLeft._1, topLeft._2), Point(topLeft._1+width, topLeft._2+height))
}

class Rectangle(val topLeft:Point, val bottomRight:Point) {

  def width = math.abs(bottomRight.x - topLeft.x)
  def height = math.abs(topLeft.y - bottomRight.y)

  def minX = topLeft.x
  def maxX = bottomRight.x
  def minY = topLeft.y
  def maxY = bottomRight.y
  def /(x:Float, y:Float) = new Rectangle(
    Point(topLeft.x / x, topLeft.y / y),
    Point(bottomRight.x / x, bottomRight.y / y)
  )

  def center : (Float,Float) = (
    (topLeft.x+bottomRight.x) / 2,
    (topLeft.y+bottomRight.y) / 2
  )

  def isInside(x:Float, y:Float) : Boolean = {
    (x >= topLeft.x && x <= bottomRight.x && 
     y >= topLeft.y && y <= bottomRight.y)
  }

  override def toString = "Rectangle(" + topLeft + ", " + bottomRight + ")"
}

