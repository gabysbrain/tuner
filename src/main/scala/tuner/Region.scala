package tuner

import net.liftweb.json.JsonParser._
//import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

object Region {
  abstract class Shape
  case object Box extends Shape
  case object Ellipse extends Shape

  def apply(t:Shape, project:Project) = t match {
    case Box => new BoxRegion(project)
    case Ellipse => new EllipseRegion(project)
  }
}

sealed abstract class Region(project:Project) {
  var _radius:Map[String,Float] = 
    project.inputFields.map {fld => (fld -> 0f)} toMap
  
  def radius(fld:String) = _radius(fld)
  def setRadius(fld:String, v:Float) = {
    _radius += (fld -> v)
  }

  def toJson = {
    val shapeName = this match {
      case x:BoxRegion => "Box"
      case x:EllipseRegion => "Ellipse"
    }
    (
      ("shape" -> shapeName) ~
      ("radii" -> _radius.map {case (fld, v) =>
        ("field" -> fld) ~
        ("radius" -> v)
      }.toList)
    )
  }
}

class BoxRegion(project:Project) extends Region(project) {
}

class EllipseRegion(project:Project) extends Region(project) {
}
