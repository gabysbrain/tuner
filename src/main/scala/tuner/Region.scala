package tuner

import net.liftweb.json.JsonParser._
//import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

case class RadiusSpecification(field:String, radius:Float)
case class RegionSpecification(shape:String, radii:List[RadiusSpecification])

object Region {
  abstract class Shape
  case object Box extends Shape
  case object Ellipse extends Shape

  def apply(t:Shape, project:Project) = t match {
    case Box => new BoxRegion(project)
    case Ellipse => new EllipseRegion(project)
  }

  def fromJson(json:RegionSpecification, project:Project) = {
    val reg = json.shape match {
      case "Box"     => Region(Box, project)
      case "Ellipse" => Region(Ellipse, project)
    }
    json.radii.foreach {r =>
      reg.setRadius(r.field, r.radius)
    }
    reg
  }
}

sealed abstract class Region(project:Project) {
  var _radius:Map[String,Float] = 
    project.inputFields.map {fld => 
      //val (low, high) = project.inputs.range(fld)
      (fld -> 0f)
    } toMap
  
  def radius(fld:String) = _radius(fld)
  def setRadius(fld:String, v:Float) = {
    _radius += (fld -> v)
  }

  def center = project.currentSlice

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

  def inside(pt:List[(String,Float)]) : Boolean

  def numSamples = {
    var count = 0
    for(i <- 0 until project.samples.numRows) {
      val tpl = project.samples.tuple(i)
      if(inside(tpl.toList))
        count += 1
    }
    count
  }

  def gradient(response:String, fld:String) = {
    val models = project.gpModels.get
    val model = models(response)
    val minVal = center(fld) - radius(fld)
    val maxVal = center(fld) + radius(fld)
    val rest = center.toList.filter(_._1 != fld)
    val (minPt, maxPt) = ((fld, minVal) :: rest, (fld, maxVal) :: rest)
    val (minEst, maxEst) = (model.runSample(minPt)._1, 
                            model.runSample(maxPt)._1)
    val centerEst = model.runSample(center.toList)._1
    math.max((centerEst - minEst)/radius(fld), 
             (centerEst - maxEst)/radius(fld))
  }
}

class BoxRegion(project:Project) extends Region(project) {
  def inside(pt:List[(String,Float)]) : Boolean = {
    val center = project.currentSlice
    pt.foldLeft(true) {case (bool,(fld,value)) =>
      val (minVal, maxVal) = (center(fld)-radius(fld), center(fld)+radius(fld))
      bool && value >= minVal && value <= maxVal
    }
  }

}

class EllipseRegion(project:Project) extends Region(project) {
  // TODO: fix this.  It's wrong!!!
  def inside(pt:List[(String,Float)]) : Boolean = {
    val center = project.currentSlice
    pt.foldLeft(true) {case (bool,(fld,value)) =>
      val (minVal, maxVal) = (center(fld)-radius(fld), center(fld)+radius(fld))
      bool && value >= minVal && value <= maxVal
    }
  }

}

