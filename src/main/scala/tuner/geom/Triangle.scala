package tuner.geom

object Triangle {
  
  def apply(point1:Point, point2:Point, point3:Point) : Triangle = 
    new Triangle(point1, point2, point3)
  def apply(point1:(Float,Float), point2:(Float,Float), point3:(Float,Float)) : Triangle =
    apply(Point(point1._1, point1._2), 
          Point(point2._1, point2._2),
          Point(point3._1, point3._2))
}

class Triangle(val point1:Point, val point2:Point, val point3:Point) {

  def width = maxX - minX
  def height = maxY - minY

  def minX = math.min(point1.x, math.min(point2.x, point3.x))
  def maxX = math.max(point1.x, math.max(point2.x, point3.x))
  def minY = math.min(point1.y, math.min(point2.y, point3.y))
  def maxY = math.max(point1.y, math.max(point2.y, point3.y))

  def center : (Float,Float) = (
    (minX+maxX) / 2, (minY+maxY) / 2
  )

  def isInside(x:Float, y:Float) : Boolean = {
    (x >= minX && x <= maxX && 
     y >= minY && y <= maxY)
  }
}

