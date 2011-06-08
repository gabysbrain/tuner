package tuner.gui

import tuner.BoxRegion
import tuner.ColorMap
import tuner.Config
import tuner.DimRanges
import tuner.EllipseRegion
import tuner.GpModel
import tuner.Matrix2D
import tuner.Project
import tuner.SpecifiedColorMap
import tuner.geom.Rectangle
import tuner.gui.event.HistoryAdd
import tuner.gui.event.SliceChanged
import tuner.gui.util.AxisTicks
import tuner.gui.util.FacetLayout
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Colorbar
import tuner.gui.widgets.ContinuousPlot
import tuner.util.ColorLib

import scala.swing.Publisher

import processing.core.PConstants

class MainPlotPanel(project:Project) extends P5Panel(Config.mainPlotDims._1, 
                                                     Config.mainPlotDims._2, 
                                                     P5Panel.P3D) 
                                     with Publisher {

  type PlotInfoMap = Map[(String,String), ContinuousPlot]
  type AxisMap = Map[String,Axis]
  type ColormapMap = Map[String,(SpecifiedColorMap,SpecifiedColorMap,SpecifiedColorMap)]
  // This is the response field, gp model, x axes, y axes, and plots
  //type ResponseInfo = (String,GpModel,AxisMap,AxisMap,Colorbar,PlotInfoMap)


  // Everything response 1 needs 
  val resp1Colormaps = createColormaps(Config.response1ColorMap)
  val resp1Colorbar:Colorbar = new Colorbar(Colorbar.Left)
  val resp1XAxes = createAxes(Axis.HorizontalBottom)
  val resp1YAxes = createAxes(Axis.VerticalLeft)
  val resp1Plots = createPlots

  // Everything response 2 needs
  val resp2Colormaps = createColormaps(Config.response2ColorMap)
  val resp2Colorbar:Colorbar = new Colorbar(Colorbar.Right)
  val resp2XAxes = createAxes(Axis.HorizontalTop)
  val resp2YAxes = createAxes(Axis.VerticalRight)
  val resp2Plots = createPlots

  // Cache a bunch of statistics on where the plots are for hit detection
  var leftColorbarBounds = Rectangle((0f,0f), 0f, 0f)
  var rightColorbarBounds = Rectangle((0f,0f), 0f, 0f)
  var topAxisBounds = Rectangle((0f,0f), 0f, 0f)
  var bottomAxisBounds = Rectangle((0f,0f), 0f, 0f)
  var leftAxisBounds = Rectangle((0f,0f), 0f, 0f)
  var rightAxisBounds = Rectangle((0f,0f), 0f, 0f)
  var plotBounds = Rectangle((0f,0f), 0f, 0f)
  var sliceBounds = Map[(String,String),Rectangle]()
  var sliceSize = 0f

  def colormap(response:String, map:ColormapMap) : SpecifiedColorMap = {
    val (value, error, gain) = map(response)
    project.viewInfo.currentMetric match {
      case Project.ValueMetric => value
      case Project.ErrorMetric => error
      case Project.GainMetric => gain
    }
  }

  def plotData(model:GpModel,
               d1:(String,(Float,Float)), 
               d2:(String,(Float,Float)), 
               slice:Map[String,Float]) : Matrix2D = {
    val sample = model.sampleSlice(d1, d2, slice.toList)
    val data = project.viewInfo.currentMetric match {
      case Project.ValueMetric => sample._1
      case Project.ErrorMetric => sample._2
      case Project.GainMetric => sample._3
    }
    data._2
  }

  def draw = {
    applet.background(Config.backgroundColor)

    // Compute the spacing of everything
    val startTime = System.currentTimeMillis

    // Update all the bounding boxes
    updateBounds
    val (ss, sb) = FacetLayout.plotBounds(plotBounds, project.inputFields)
    sliceSize = ss
    sliceBounds = sb

    // We might need to draw the region mask
    

    // First see if we're drawing the colorbars
    project.viewInfo.response1View.foreach {r =>
      resp1Colorbar.draw(this, leftColorbarBounds.minX, 
                               leftColorbarBounds.minY,
                               leftColorbarBounds.width, 
                               leftColorbarBounds.height,
                               r, colormap(r, resp1Colormaps))
    }
    project.viewInfo.response2View.foreach {r =>
      resp2Colorbar.draw(this, rightColorbarBounds.minX, 
                               rightColorbarBounds.minY,
                               rightColorbarBounds.width, 
                               rightColorbarBounds.height,
                               r, colormap(r, resp2Colormaps))
    }

    drawResponses

    val endTime = System.currentTimeMillis
    //println("draw time: " + (endTime - startTime) + "ms")
  }

  private def updateBounds = {
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

  private def drawResponses = {
    // Loop through all field combinations to see what we need to draw
    project.inputFields.foreach {xFld =>
      project.inputFields.foreach {yFld =>
        val xRange = (xFld, project.viewInfo.currentZoom.range(xFld))
        val yRange = (yFld, project.viewInfo.currentZoom.range(yFld))

        if(xFld < yFld) {
          project.viewInfo.response1View.foreach {r1 => 
            val startTime = System.currentTimeMillis
            drawResponse(xFld, yFld, xRange, yRange, r1)
            if(project.viewInfo.showRegion)
              drawMask(xFld, yFld)
            val endTime = System.currentTimeMillis
            //println("r1 draw time: " + (endTime-startTime) + "ms")
          }
        } else if(xFld > yFld) {
          project.viewInfo.response2View.foreach {r2 =>
            val startTime = System.currentTimeMillis
            drawResponse(xFld, yFld, xRange, yRange, r2)
            if(project.viewInfo.showRegion)
              drawMask(xFld, yFld)
            val endTime = System.currentTimeMillis
            //println("r2 draw time: " + (endTime-startTime) + "ms")
          }
        } else {
          drawAxes(xRange)
        }
      }
    }
  }

  private def drawResponse(xFld:String, yFld:String, 
                           xRange:(String,(Float,Float)), 
                           yRange:(String,(Float,Float)), 
                           response:String) = {

    project.gpModels foreach {gpm =>
      val model = gpm(response)
      val bounds = sliceBounds((xFld, yFld))
      val (slice, cm, xf, yf, xr, yr) = if(xFld < yFld) {
        (resp1Plots((xFld, yFld)), colormap(response, resp1Colormaps),
         xFld, yFld, xRange, yRange)
      } else {
        (resp2Plots((yFld, xFld)), colormap(response, resp2Colormaps),
         yFld, xFld, yRange, xRange)
      }

      val data = plotData(model, xr, yr, project.viewInfo.currentSlice)
      val (xSlice, ySlice) = (project.viewInfo.currentSlice(xf), 
                              project.viewInfo.currentSlice(yf))

      // Draw the main plot
      slice.draw(this, bounds.minX, bounds.minY, bounds.width, bounds.height,
                 data, xSlice, ySlice, xr._2, yr._2, cm)
    }
  }

  private def drawAxes(range:(String,(Float,Float))) = {
    val (fld, (low, high)) = range
    val firstField = project.inputFields.head
    val lastField = project.inputFields.last
    val ticks = AxisTicks.ticks(low, high)

    project.viewInfo.response1View.foreach {r1 =>
      // See if we draw the x axis
      if(fld != lastField) {
        val sliceDim = sliceBounds((fld, lastField))
        val axis = resp1XAxes(fld)
        axis.draw(this, sliceDim.minX, bottomAxisBounds.minY, 
                        sliceDim.width, bottomAxisBounds.height, 
                        fld, ticks)
      }
      // See if we draw the y axis
      if(fld != firstField) {
        val sliceDim = sliceBounds((firstField, fld))
        val axis = resp1YAxes(fld)
        axis.draw(this, leftAxisBounds.minX, sliceDim.minY, 
                        leftAxisBounds.width, sliceDim.height, 
                        fld, ticks)
      }
    }
    project.viewInfo.response2View.foreach {r2 =>
      // See if we draw the x axis
      if(fld != lastField) {
        val sliceDim = sliceBounds((lastField, fld))
        val axis = resp2XAxes(fld)
        axis.draw(this, sliceDim.minX, topAxisBounds.minY, 
                        sliceDim.width, topAxisBounds.height, 
                        fld, ticks)
      }
      // See if we draw the y axis
      if(fld != firstField) {
        val sliceDim = sliceBounds((fld, firstField))
        val axis = resp2YAxes(fld)
        axis.draw(this, rightAxisBounds.minX, sliceDim.minY, 
                        rightAxisBounds.width, sliceDim.height, 
                        fld, ticks)
      }
    }
  }

  private def drawMask(xFld:String, yFld:String) = {
    val slice = sliceBounds((xFld, yFld))

    pushMatrix
    translate(slice.minX, slice.minY, 1)

    val (xf, yf) = if(xFld < yFld) (xFld, yFld)
                   else            (yFld, xFld)
    val (xZoom, yZoom) = (project.viewInfo.currentZoom.range(xf),
                          project.viewInfo.currentZoom.range(yf))

    val (xMinRng, xMaxRng) = project.region.range(xf)
    val (yMinRng, yMaxRng) = project.region.range(yf)

    // Convert the x and y ranges into pixel values
    val xxMin = P5Panel.constrain(
      P5Panel.map(xMinRng, xZoom._1, xZoom._2, 0, slice.width),
      0, slice.width)
    val xxMax = P5Panel.constrain(
      P5Panel.map(xMaxRng, xZoom._1, xZoom._2, 0, slice.width),
      0, slice.width)
    val yyMin = P5Panel.constrain(
      P5Panel.map(yMinRng, yZoom._2, yZoom._1, 0, slice.height),
      0, slice.height)
    val yyMax = P5Panel.constrain(
      P5Panel.map(yMaxRng, yZoom._2, yZoom._1, 0, slice.height),
      0, slice.height)

    fill(Config.regionColor)
    stroke(ColorLib.darker(Config.regionColor))

    project.region match {
      case _:BoxRegion =>
        rectMode(P5Panel.RectMode.Corners)
        rect(xxMin, yyMin, xxMax, yyMax)
        //println(xSlice + " " + ySlice + " " + xRad + " " + yRad)
      case _:EllipseRegion =>
        ellipseMode(P5Panel.EllipseMode.Corners)
        ellipse(xxMin, yyMin, xxMax, yyMax)
    }

    popMatrix
  }

  private def createPlots : PlotInfoMap = {
    project.inputFields.flatMap({fld1 =>
      project.inputFields.flatMap({fld2 =>
        if(fld1 < fld2) {
          Some((fld1, fld2), new ContinuousPlot)
        } else {
          None
        }
      })
    }).toMap
  }
  
  private def createAxes(position:Axis.Placement) = {
    val fields = position match {
      case Axis.HorizontalTop | Axis.HorizontalBottom => 
        project.inputFields.init
      case Axis.VerticalLeft | Axis.VerticalRight => 
        project.inputFields.tail
    }
    fields.map {fld => (fld, new Axis(position))} toMap
  }

  private def createColormaps(respColormap:ColorMap) : ColormapMap = {
    project.responseFields.flatMap {fld =>
      project.gpModels.map {gpm =>
        val model = gpm(fld)
        val valCm = new SpecifiedColorMap(respColormap,
                                          model.funcMin, 
                                          model.funcMax)
        val errCm = new SpecifiedColorMap(Config.errorColorMap,
                                          0f, model.sig2.toFloat)
        // TODO: fix the max gain calculation!
        val gainCm = new SpecifiedColorMap(Config.gainColorMap, 0f, 1f)
        (fld, (valCm, errCm, gainCm))
      }
    } toMap
  }

  override def mouseDragged(prevMouseX:Int, prevMouseY:Int, 
                            mouseX:Int, mouseY:Int,
                            button:P5Panel.MouseButton.Value) = {
    // Now figure out if we need to deal with any mouse 
    // movements in the colorbars
    if(mouseButton == P5Panel.MouseButton.Left) {
      //val (mouseX, mouseY) = mousePos
      handleBarMouse(mouseX, mouseY)
      handlePlotMouse(mouseX, mouseY)
    }
  }

  override def mouseClicked(mouseX:Int, mouseY:Int, 
                            button:P5Panel.MouseButton.Value) = {
    // Now figure out if we need to deal with any mouse 
    // movements in the colorbars
    if(button == P5Panel.MouseButton.Left) {
      //val (mouseX, mouseY) = mousePos
      if(keyCode == P5Panel.KeyCode.Shift) {
        publish(new HistoryAdd(this, project.viewInfo.currentSlice.toList))
      } else {
        handleBarMouse(mouseX, mouseY)
        handlePlotMouse(mouseX, mouseY)
      }
    }
  }

  def handlePlotMouse(mouseX:Int, mouseY:Int) = {

    // Do a rough check to see if we're near any of the slicess
    if(plotBounds.isInside(mouseX, mouseY)) {
      sliceBounds.foreach {case ((xfld,yfld), sb) =>
        if(sb.isInside(mouseX, mouseY)) {
          // Make sure we're inside a bounds that's active
          if(xfld < yfld && project.viewInfo.response1View.isDefined) {
            val (lowZoomX, highZoomX) = project.viewInfo.currentZoom.range(xfld)
            val (lowZoomY, highZoomY) = project.viewInfo.currentZoom.range(yfld)
            val newX = P5Panel.map(mouseX, sb.minX, sb.maxX,
                                           lowZoomX, highZoomX)
            val newY = P5Panel.map(mouseY, sb.minY, sb.maxY,
                                           highZoomY, lowZoomY)
            publish(new SliceChanged(this, List((xfld, newX), (yfld, newY))))
          } else if(xfld > yfld && project.viewInfo.response2View.isDefined) {
            // x and y fields are reversed here!!!
            val (lowZoomX, highZoomX) = project.viewInfo.currentZoom.range(yfld)
            val (lowZoomY, highZoomY) = project.viewInfo.currentZoom.range(xfld)
            val newX = P5Panel.map(mouseX, sb.minX, sb.maxX,
                                           lowZoomX, highZoomX)
            val newY = P5Panel.map(mouseY, sb.minY, sb.maxY,
                                           highZoomY, lowZoomY)
            publish(new SliceChanged(this, List((yfld, newX), (xfld, newY))))
          }
        }
      }
    }
  }

  def handleBarMouse(mouseX:Int, mouseY:Int) = {
    if(leftColorbarBounds.isInside(mouseX, mouseY)) {
      project.viewInfo.response1View.foreach {r1 =>
        val cb = resp1Colorbar
        val cm = colormap(r1, resp1Colormaps)
        val filterVal = P5Panel.map(mouseY, cb.barBounds.maxY, 
                                            cb.barBounds.minY, 
                                            cm.minVal, 
                                            cm.maxVal)
        cm.filterVal = filterVal
      }
    }
    if(rightColorbarBounds.isInside(mouseX, mouseY)) {
      project.viewInfo.response2View.foreach {r2 =>
        val cb = resp2Colorbar
        val cm = colormap(r2, resp2Colormaps)
        val filterVal = P5Panel.map(mouseY, cb.barBounds.maxY, 
                                            cb.barBounds.minY, 
                                            cm.minVal, 
                                            cm.maxVal)
        cm.filterVal = filterVal
      }
    }
  }
}

