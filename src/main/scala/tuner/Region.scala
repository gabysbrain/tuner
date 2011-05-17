package tuner

object Region {
  abstract class Shape
  case object Box extends Shape
  case object Ellipse extends Shape

  def apply(t:Shape) = t match {
    case Box => new BoxRegion
    case Ellipse => new EllipseRegion
  }

}

sealed trait Region {
}

class BoxRegion extends Region {
}

class EllipseRegion extends Region {
}

