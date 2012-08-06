package tuner

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import tuner.project.Viewable

case class SliceSpecification(name:String, value:Float)
case class ZoomSpecification(name:String, lowValue:Float, highValue:Float)
case class VisInfo(
  currentSlice:List[SliceSpecification],
  currentZoom:List[ZoomSpecification],
  response1:Option[String],
  response2:Option[String],
  currentVis:Option[String], // Not in v1 of config
  currentMetric:String,
  showSampleLine:Boolean,
  showRegion:Boolean
)

/**
 * An object to save the state of the current view in one place.
 *
 * Includes things like focus point, zoom level, etc.
 */
object ViewInfo {
  val DefaultVisInfo = VisInfo (
    Nil, Nil, None, None, Some("hyperslice"), "value", false, false
  )

  def fromJson(project:Viewable, vi:VisInfo) = {
    val v = new ViewInfo(project)
    v._currentSlice = vi.currentSlice.map({x => 
      (x.name, x.value)
    }).toMap
    v._currentZoom = new DimRanges(vi.currentZoom.map {x => 
      (x.name, (x.lowValue, x.highValue))
    } toMap)
    v.response1View = vi.response1
    v.response2View = vi.response2
    v.currentVis = vi.currentVis match {
      case Some("hyperslice") => Hyperslice
      case Some("splom") => Splom
      case None => Hyperslice
    }
    v.currentMetric = vi.currentMetric match {
      case "value" => ValueMetric
      case "error" => ErrorMetric
      case "gain" => GainMetric
    }
    v.showSampleLine = vi.showSampleLine
    v.showRegion = vi.showRegion
    v
  }

  sealed trait Vis
  case object Hyperslice extends Vis
  case object Splom extends Vis

  sealed trait MetricView
  case object ValueMetric extends MetricView
  case object ErrorMetric extends MetricView
  case object GainMetric extends MetricView
}

class ViewInfo(project:Viewable) {

  import ViewInfo._

  //var _currentSlice = Map[String,Float]()
  //var _currentZoom:DimRanges = new DimRanges(Nil.toMap)
  var _currentSlice = project.inputs.dimNames.map {fld =>
    val (mn, mx) = project.inputs.range(fld)
    (fld, (mn+mx)/2)
  } toMap
  var _currentZoom = new DimRanges(project.inputs.dimNames.map {fld =>
    (fld, project.inputs.range(fld))
  } toMap)
  var response1View:Option[String] = None
  var response2View:Option[String] = None
  var currentVis:Vis = Hyperslice
  var currentMetric:MetricView = ValueMetric
  var showSampleLine = false
  var showRegion = true

  val estimateSampleDensity:Int = {
    val len = project.inputFields.length
    val numPlots = math.pow(len, 2) - len
    math.sqrt(Config.maxEstimateSamples / numPlots).toInt
  }

  def currentSlice : Map[String,Float] = {
    // Pick defaults for any missing dimensions
    project.inputs.dimNames.toSet.diff(_currentSlice.keySet).foreach {k =>
      val (min, max) = project.inputs.range(k)
      _currentSlice += (k -> ((min+max) / 2f))
    }
    _currentSlice
  }

  def currentZoom : DimRanges = {
    project.inputs.dimNames.toSet.diff(_currentZoom.keySet).foreach {k =>
      _currentZoom.update(k, project.inputs.min(k), project.inputs.max(k))
    }
    _currentZoom
  }

  def updateSlice(fld:String, v:Float) = {
    _currentSlice += (fld -> v)
  }

  /**
   * Method called to update the zoom level.  Will adjust the slice position
   * to stay within the zoom level if necessary
   */
  def updateZoom(fld:String, low:Float, high:Float) = {
    _currentZoom.update(fld, low, high)

    // Ensure that the slice position is within the new bounds
    updateSlice(fld, math.max(math.min(currentSlice(fld), high), low))
  }

  def inView(pt:List[(String,Float)]) : Boolean = pt.forall {case (fld,v) =>
    !currentZoom.contains(fld) || {
      val (mn, mx) = currentZoom.range(fld)
      v >= mn && v <= mx
    }
  }

  def toJson = {
    val sliceList = currentSlice.map {case (fld,v) => 
      SliceSpecification(fld, v)
    } toList
    val zoomList = currentZoom.dimNames.map {fld =>
      ZoomSpecification(fld, currentZoom.min(fld), currentZoom.max(fld))
    } toList
    val strVis = currentVis match {
      case Hyperslice => "hyperslice"
      case Splom => "splom"
    }
    val strMetric = currentMetric match {
      case ValueMetric => "value"
      case ErrorMetric => "error"
      case GainMetric => "gain"
    }

    VisInfo(
      sliceList,
      zoomList,
      response1View,
      response2View,
      Some(strVis),
      strMetric,
      showSampleLine,
      showRegion)
  }
}

