package tuner.gui

import tuner.BoxRegion
import tuner.ColorMap
import tuner.Config
import tuner.DimRanges
import tuner.EllipseRegion
import tuner.GpModel
import tuner.Matrix2D
import tuner.SpecifiedColorMap
import tuner.Table
import tuner.ViewInfo
import tuner.geom.Rectangle
import tuner.gui.util.AxisTicks
import tuner.gui.util.FacetLayout
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Colorbar
import tuner.gui.widgets.ContinuousPlot
import tuner.gui.widgets.Widgets
import tuner.project.Viewable
import tuner.util.ColorLib

import scala.collection.mutable.Queue
import scala.swing.event.UIElementMoved
import scala.swing.event.UIElementResized

import processing.core.PConstants

/**
 * A Hyperslice matrix implemented using the Processing API
 */
class ProcessingMainPlotPanel(val project:Viewable) 
    extends P5Panel(Config.mainPlotDims._1, 
                    Config.mainPlotDims._2, 
                    P5Panel.OpenGL) 
    with MainPlotPanel {

  type PlotInfoMap = Map[(String,String), ContinuousPlot]
  type AxisMap = Map[String,Axis]

  // Everything response 1 needs 
  val resp1Colorbar:Colorbar = new Colorbar(Colorbar.Left)
  val resp1XAxes = createAxes(Axis.HorizontalBottom)
  val resp1YAxes = createAxes(Axis.VerticalLeft)
  val resp1Plots = createPlots

  // Everything response 2 needs
  val resp2Colorbar:Colorbar = new Colorbar(Colorbar.Right)
  val resp2XAxes = createAxes(Axis.HorizontalTop)
  val resp2YAxes = createAxes(Axis.VerticalRight)
  val resp2Plots = createPlots

  // Cache a bunch of statistics on where the plots are for hit detection
  var mousedPlot:Option[(String,String)] = None

  reactions += {
    case UIElementMoved(_) => 
      clearFonts
    case UIElementResized(_) => 
      clearFonts
  }

  override def setup = {
    super.setup
    loop = false
  }

  def redraw = applet.loop

  def draw = {
    loop = false

    // Need to clear the font cache when resizing.  
    // Otherwise wakiness will ensue
    if((width, height) != Config.mainPlotDims) {
      clearFonts
    }
    applet.background(Config.backgroundColor)

    // Update all the bounding boxes
    val bbStartTime = System.currentTimeMillis
    updateBounds(width, height)
    val (ss, sb) = FacetLayout.plotBounds(plotBounds, project.inputFields)
    sliceSize = ss
    sliceBounds = sb
    val bbEndTime = System.currentTimeMillis

    // See if we should highlight the 2 plots
    val phStartTime = System.currentTimeMillis
    mousedPlot.foreach {case (fld1, fld2) => drawPlotHighlight(fld1, fld2)}
    val phEndTime = System.currentTimeMillis

    // Draw the colorbars
    val cbStartTime = System.currentTimeMillis
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
    val cbEndTime = System.currentTimeMillis

    // Draw the axes
    val axStartTime = System.currentTimeMillis
    project.inputFields.foreach {fld =>
      val rng = (fld, project.viewInfo.currentZoom.range(fld))
      drawAxes(rng)
    }
    val axEndTime = System.currentTimeMillis

    // Draw the responses
    val rrStartTime = System.currentTimeMillis
    drawResponses
    val rrEndTime = System.currentTimeMillis

    // Draw the fps counter
    //drawRenderTime(endTime-startTime)
  }

  private def drawPlotHighlight(field1:String, field2:String) = {
    val bounds1 = sliceBounds((field1, field2))
    val bounds2 = sliceBounds((field2, field1))
    val offset = Config.plotSpacing / 2f
    noStroke
    rectMode(P5Panel.RectMode.Corners)

    List(bounds1, bounds2).foreach {bounds =>
      fill(0)
      rect(bounds.minX-offset, bounds.minY-offset, 
           bounds.maxX+offset, bounds.maxY+offset)
      fill(255)
      rect(bounds.minX-1, bounds.minY-1, 
           bounds.maxX+1, bounds.maxY+1)
    }
  }

  protected def drawResponses = {
    // Find the closest sample
    val closestSample = project.closestSample(
      project.viewInfo.currentSlice.toList).toMap

    // Loop through all field combinations to see what we need to draw
    project.inputFields.foreach {xFld =>
      project.inputFields.foreach {yFld =>
        val xRange = (xFld, project.viewInfo.currentZoom.range(xFld))
        val yRange = (yFld, project.viewInfo.currentZoom.range(yFld))

        if(xFld < yFld) {
          project.viewInfo.response1View.foreach {r1 => 
            val startTime = System.currentTimeMillis
            drawResponse(xRange, yRange, r1)
            drawResponseWidgets(xRange, yRange, closestSample)
            val endTime = System.currentTimeMillis
            //println("r1 draw time: " + (endTime-startTime) + "ms")
          }
        } else if(xFld > yFld) {
          project.viewInfo.response2View.foreach {r2 =>
            val startTime = System.currentTimeMillis
            drawResponse(xRange, yRange, r2)
            drawResponseWidgets(xRange, yRange, closestSample)
            val endTime = System.currentTimeMillis
            //println("r2 draw time: " + (endTime-startTime) + "ms")
          }
        }
      }
    }
  }

  protected def drawResponse(xRange:(String,(Float,Float)), 
                             yRange:(String,(Float,Float)), 
                             response:String) = {

    val (xFld, yFld) = (xRange._1, yRange._1)
    val bounds = sliceBounds((xFld, yFld))
    val (slice, cm, xf, yf, xr, yr) = if(xFld < yFld) {
      (resp1Plots((xFld, yFld)), colormap(response, resp1Colormaps),
       xFld, yFld, xRange, yRange)
    } else {
      (resp2Plots((yFld, xFld)), colormap(response, resp2Colormaps),
       yFld, xFld, yRange, xRange)
    }

    val data = project.sampleMatrix(xr, yr, response, 
                                    project.viewInfo.currentSlice.toList)
    val (xSlice, ySlice) = (project.viewInfo.currentSlice(xf), 
                            project.viewInfo.currentSlice(yf))

    // Draw the main plot
    slice.draw(this, bounds.minX, bounds.minY, bounds.width, bounds.height,
               data, xSlice, ySlice, xr._2, yr._2, cm)
  }

  protected def drawResponseWidgets(xRange:(String,(Float,Float)),
                                    yRange:(String,(Float,Float)),
                                    closestSample:Table.Tuple) = {
    val (xFld, yFld) = (xRange._1, yRange._1)
    val bounds = sliceBounds((xFld, yFld))
    val (xf, yf, xr, yr) = if(xFld < yFld) {
      (xFld, yFld, xRange, yRange)
    } else {
      (yFld, xFld, yRange, xRange)
    }

    val (xSlice, ySlice) = (project.viewInfo.currentSlice(xf), 
                            project.viewInfo.currentSlice(yf))

    // Crosshair showing current location
    Widgets.crosshair(this, bounds.minX, bounds.minY, 
                            bounds.width, bounds.height,
                            xSlice, ySlice, xr._2, yr._2)

    // Line to the nearest sample
    if(project.viewInfo.showSampleLine) {
      Widgets.sampleLine(this, bounds.minX, bounds.minY,
                               bounds.width, bounds.height,
                               xSlice, ySlice, 
                               closestSample(xf), closestSample(yf),
                               xr._2, yr._2)
    }

    // The region mask
    if(project.viewInfo.showRegion)
      drawMask(xFld, yFld)
  }

  private def drawAxes(range:(String,(Float,Float))) = {
    val (fld, (low, high)) = range
    val firstField = project.inputFields.head
    val lastField = project.inputFields.last

    project.viewInfo.response1View.foreach {r1 =>
      // See if we draw the x axis
      if(fld != lastField) {
        val sliceDim = sliceBounds((fld, lastField))
        val axis = resp1XAxes(fld)
        val ticks = AxisTicks.ticks(low, high, 
                                    sliceDim.width, 
                                    Config.smallFontSize)
        axis.draw(this, sliceDim.minX, bottomAxisBounds.minY, 
                        sliceDim.width, bottomAxisBounds.height, 
                        fld, ticks)
      }
      // See if we draw the y axis
      if(fld != firstField) {
        val sliceDim = sliceBounds((firstField, fld))
        val axis = resp1YAxes(fld)
        val ticks = AxisTicks.ticks(low, high, 
                                    sliceDim.height, 
                                    Config.smallFontSize)
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
        val ticks = AxisTicks.ticks(low, high, 
                                    sliceDim.width, 
                                    Config.smallFontSize)
        axis.draw(this, sliceDim.minX, topAxisBounds.minY, 
                        sliceDim.width, topAxisBounds.height, 
                        fld, ticks)
      }
      // See if we draw the y axis
      if(fld != firstField) {
        val sliceDim = sliceBounds((fld, firstField))
        val axis = resp2YAxes(fld)
        val ticks = AxisTicks.ticks(low, high, 
                                    sliceDim.height, 
                                    Config.smallFontSize)
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

  private def drawRenderTime = {
    // Draw an fps counter in the lower right
    textAlign(P5Panel.TextHAlign.Right, P5Panel.TextVAlign.Bottom)
    textFont(Config.fontPath, 16)
    fill(255)
    //val ft = drawTimes.sum / drawTimes.size.toDouble
    val ft = 0.0
    text("Draw time: " + ft + "ms", width-10, height-10)
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

  override def mouseMoved(prevMouseX:Int, prevMouseY:Int, 
                          mouseX:Int, mouseY:Int, 
                          button:P5Panel.MouseButton.Value) = {
    val pb = sliceBounds.find {case (_,b) => b.isInside(mouseX, mouseY)}
    val newPos = pb.map {tmp => tmp._1}
    if(newPos != mousedPlot) {
      mousedPlot = newPos
      redraw
    }
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
      redraw
    }
  }

  override def mouseClicked(mouseX:Int, mouseY:Int, 
                            button:P5Panel.MouseButton.Value) = {
    // Now figure out if we need to deal with any mouse 
    // movements in the colorbars
    if(button == P5Panel.MouseButton.Left) {
      handleBarMouse(mouseX, mouseY)
      handlePlotMouse(mouseX, mouseY)
      redraw
    }
  }

  override def keyPressed(key:Char) = {
    if(key == 'h' || key == 'H') {
      publishHistoryAdd(this)
    }
  }

  /**
   * What to do when the mouse is clicked inside the bounds 
   * of the hyperslice matrix
   */
  def handlePlotMouse(mouseX:Int, mouseY:Int) = {

    // Do a rough check to see if we're near any of the slices
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
            publishSliceChanged(this, (xfld, newX), (yfld, newY))
          } else if(xfld > yfld && project.viewInfo.response2View.isDefined) {
            // x and y fields are reversed here!!!
            val (lowZoomX, highZoomX) = project.viewInfo.currentZoom.range(yfld)
            val (lowZoomY, highZoomY) = project.viewInfo.currentZoom.range(xfld)
            val newX = P5Panel.map(mouseX, sb.minX, sb.maxX,
                                           lowZoomX, highZoomX)
            val newY = P5Panel.map(mouseY, sb.minY, sb.maxY,
                                           highZoomY, lowZoomY)
            publishSliceChanged(this, (yfld, newX), (xfld, newY))
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

