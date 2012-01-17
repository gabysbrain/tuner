package tuner.gui

import tuner.Config
import tuner.ColorMap
import tuner.SpecifiedColorMap
import tuner.ViewInfo
import tuner.geom.Rectangle
import tuner.gui.event.HistoryAdd
import tuner.gui.event.SliceChanged
import tuner.project.Viewable

import scala.swing.Publisher

trait MainPlotPanel extends Publisher {

  type ColormapMap = Map[String,(SpecifiedColorMap,SpecifiedColorMap,SpecifiedColorMap)]

  val project:Viewable

  // The colormaps
  val resp1Colormaps = createColormaps(Config.response1ColorMap)
  val resp2Colormaps = createColormaps(Config.response2ColorMap)

  // cached bounds for all the elements
  var leftColorbarBounds = Rectangle((0f,0f), 0f, 0f)
  var rightColorbarBounds = Rectangle((0f,0f), 0f, 0f)
  var topAxisBounds = Rectangle((0f,0f), 0f, 0f)
  var bottomAxisBounds = Rectangle((0f,0f), 0f, 0f)
  var leftAxisBounds = Rectangle((0f,0f), 0f, 0f)
  var rightAxisBounds = Rectangle((0f,0f), 0f, 0f)
  var plotBounds = Rectangle((0f,0f), 0f, 0f)
  var sliceBounds = Map[(String,String),Rectangle]()
  var sliceSize = 0f
  
  def redraw : Unit

  def colormap(response:String, map:ColormapMap) : SpecifiedColorMap = {
    val (value, error, gain) = map(response)
    project.viewInfo.currentMetric match {
      case ViewInfo.ValueMetric => value
      case ViewInfo.ErrorMetric => error
      case ViewInfo.GainMetric => gain
    }
  }

  def publishHistoryAdd(sender:scala.swing.Component) =
    publish(new HistoryAdd(sender, project.viewInfo.currentSlice.toList))

  def publishSliceChanged(sender:scala.swing.Component,
                          xChange:(String,Float), yChange:(String,Float)) =
    publish(new SliceChanged(sender, List(xChange, yChange)))

  protected def createColormaps(respColormap:ColorMap) : ColormapMap = {
    project.responses.map {case (fld, minimize) =>
      val model = project.gpModels(fld)
      val valCm = new SpecifiedColorMap(respColormap,
                                        model.funcMin, 
                                        model.funcMax,
                                        minimize)
      val errCm = new SpecifiedColorMap(Config.errorColorMap,
                                        0f, model.sig2.toFloat,
                                        false)
      // TODO: fix the max gain calculation!
      val maxGain = model.maxGain(project.inputs) / 4
      val gainCm = new SpecifiedColorMap(Config.gainColorMap, 0f, 
                                         maxGain, false)
      (fld, (valCm, errCm, gainCm))
    } toMap
  }


  protected def updateBounds(width:Float, height:Float) = {
    val maxResponseWidth = width -
      ((project.viewInfo.currentZoom.length-1) * Config.plotSpacing) -
      (Config.axisSize * 2) -
      (Config.plotSpacing * 2) -
      (Config.colorbarSpacing * 4) -
      (Config.colorbarWidth * 2)
    val maxResponseHeight = height - 
      ((project.viewInfo.currentZoom.length-1) * Config.plotSpacing) -
      (Config.axisSize * 2) -
      (Config.plotSpacing * 2)
    val responseSize = math.min(maxResponseWidth, maxResponseHeight)

    // Now space everything out top to bottom, left to right
    leftColorbarBounds = Rectangle((Config.colorbarSpacing, 
                                    Config.plotSpacing+Config.axisSize),
                                   Config.colorbarWidth,
                                   responseSize)
    leftAxisBounds = Rectangle((leftColorbarBounds.maxX+Config.colorbarSpacing,
                                Config.plotSpacing+Config.axisSize),
                               Config.axisSize, responseSize)
    topAxisBounds = Rectangle((leftAxisBounds.maxX, Config.plotSpacing),
                              responseSize, Config.axisSize)
    plotBounds = Rectangle((topAxisBounds.minX, leftAxisBounds.minY),
                           responseSize, responseSize)
    bottomAxisBounds = Rectangle((plotBounds.minX, plotBounds.maxY),
                                 responseSize, Config.axisSize)
    rightAxisBounds = Rectangle((plotBounds.maxX, plotBounds.minY),
                                Config.axisSize, responseSize)
    rightColorbarBounds = Rectangle((rightAxisBounds.maxX+
                                       Config.colorbarSpacing, 
                                     rightAxisBounds.minY),
                                    Config.colorbarWidth, responseSize)
  }
}

