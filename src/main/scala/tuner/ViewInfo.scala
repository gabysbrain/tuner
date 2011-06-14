package tuner

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

case class SliceSpecification(name:String, value:Float)
case class ZoomSpecification(name:String, lowValue:Float, highValue:Float)
case class VisInfo(
  currentSlice:List[SliceSpecification],
  currentZoom:List[ZoomSpecification],
  response1:Option[String],
  response2:Option[String],
  currentMetric:String,
  showSampleLine:Boolean,
  showRegion:Boolean
)

object ViewInfo {
  def fromJson(project:Project, vi:VisInfo) = {
    val v = new ViewInfo(project)
    v._currentSlice = vi.currentSlice.map({x => 
      (x.name, x.value)
    }).toMap
    v._currentZoom = new DimRanges(vi.currentZoom.map {x => 
      (x.name, (x.lowValue, x.highValue))
    } toMap)
    v.response1View = vi.response1
    v.response2View = vi.response2
    v.currentMetric = vi.currentMetric match {
      case "value" => Project.ValueMetric
      case "error" => Project.ErrorMetric
      case "gain" => Project.GainMetric
    }
    v.showSampleLine = vi.showSampleLine
    v.showRegion = vi.showRegion
    v
  }
}

class ViewInfo(project:Project) {

  var _currentSlice = Map[String,Float]()
  var _currentZoom:DimRanges = new DimRanges(Nil.toMap)
  var response1View:Option[String] = None
  var response2View:Option[String] = None
  var currentMetric:Project.MetricView = Project.ValueMetric
  var showSampleLine = false
  var showRegion = true

  def currentSlice : Map[String,Float] = {
    // Pick defaults for any missing dimensions
    project.inputs.dimNames.toSet.diff(_currentSlice.keySet).foreach {k =>
      val (min, max) = project.inputs.range(k)
      _currentSlice += (k -> ((min+max) / 2f))
    }
    _currentSlice
  }

  def currentZoom : DimRanges = {
    project.inputs.dimNames.toSet.diff(_currentZoom.ranges.keySet).foreach {k =>
      _currentZoom.update(k, project.inputs.min(k), project.inputs.max(k))
    }
    _currentZoom
  }

  def updateSlice(fld:String, v:Float) = {
    _currentSlice += (fld -> v)
  }

  def updateZoom(fld:String, low:Float, high:Float) = {
    _currentZoom.update(fld, low, high)
  }

  def toJson = {
    val strMetric = currentMetric match {
      case Project.ValueMetric => "value"
      case Project.ErrorMetric => "error"
      case Project.GainMetric => "gain"
    }

    ("currentSlice" -> (currentSlice.map {case (fld,v) =>
      ("name" -> fld) ~
      ("value" -> v)
    }).toList) ~
    ("currentZoom" -> (currentZoom.dimNames.map {fld =>
      ("name" -> fld) ~
      ("lowValue" -> currentZoom.min(fld)) ~
      ("highValue" -> currentZoom.max(fld))
    }).toList) ~
    ("response1" -> response1View) ~
    ("response2" -> response2View) ~
    ("currentMetric" -> strMetric) ~
    ("showSampleLine" -> showSampleLine) ~
    ("showRegion" -> showRegion)
  }
}

