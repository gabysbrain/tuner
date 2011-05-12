package tuner.geom

object Rectangle {
  
  def apply(topLeft:Point, bottomRight:Point) : Rectangle = 
    new Rectangle(topLeft, bottomRight)
  def apply(topLeft:(Float,Float), bottomRight:(Float,Float)) : Rectangle =
    apply(Point(topLeft._1, topLeft._2), Point(bottomRight._1, bottomRight._2))
}

class Rectangle(val topLeft:Point, val bottomRight:Point) {

  def width = math.abs(bottomRight.x - topLeft.x)
  def height = math.abs(topLeft.y - bottomRight.y)

  def minX = topLeft.x
  def maxX = bottomRight.x
  def minY = bottomRight.y
  def maxY = topLeft.y

  def center : (Float,Float) = (
    (topLeft.x+bottomRight.x) / 2,
    (topLeft.y+bottomRight.y) / 2
  )
}

