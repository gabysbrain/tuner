package tuner.gui

import tuner.Config
import tuner.DimRanges
import tuner.GpModel
import tuner.Matrix2D
import tuner.Project
import tuner.SpecifiedColorMap
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Colorbar
import tuner.gui.widgets.ContinuousPlot

class MainPlotPanel(project:Project, resp1:Option[String], resp2:Option[String]) 
    extends P5Panel(Config.mainPlotDims._1, Config.mainPlotDims._2, P5Panel.OpenGL) {

  type PlotInfoMap = Map[(String,String), ContinuousPlot]
  type AxisMap = Map[String,Axis]
  // This is the response field, gp model, x axes, y axes, and plots
  type ResponseInfo = (String,GpModel,AxisMap,AxisMap,Colorbar,PlotInfoMap)

  /*
  var zoomDims = new DimRanges(project.inputs.ranges)
  var currentSlice:Map[String,Float] = project.inputFields.map {fld =>
    val rng = zoomDims.range(fld)
    (fld, (rng._1 + rng._2) / 2f)
  } toMap
  */

  val resp1Info:Option[ResponseInfo] = resp1 match {
    case Some(r1) => project.gpModels match {
      case Some(gpm) => 
        val model = gpm(r1)
        val cm = new SpecifiedColorMap(Config.response1ColorMap, 
                                       model.funcMin, 
                                       model.funcMax)
        println("cm1: " + cm.minVal + " " + cm.filterVal + " " + cm.maxVal)
        Some((r1, model, createAxes(Axis.HorizontalBottom),
                         createAxes(Axis.VerticalLeft),
                         new Colorbar(cm, r1),
                         createPlots(cm)))
      case None      => None
    }
    case None     => None
  }

  val resp2Info:Option[ResponseInfo] = resp2 match {
    case Some(r2) => project.gpModels match {
      case Some(gpm) => 
        val model = gpm(r2)
        val cm = new SpecifiedColorMap(Config.response2ColorMap, 
                                       model.funcMin, 
                                       model.funcMax)
        println("cm2: " + cm.minVal + " " + cm.filterVal + " " + cm.maxVal)
        Some((r2, model, createAxes(Axis.HorizontalTop),
                         createAxes(Axis.VerticalRight),
                         new Colorbar(cm, r2),
                         createPlots(cm)))
      case None      => None
    }
    case None     => None
  }

  /*
  // Cache a bunch of statistics on where the plots are for hit detection
  var responseSize:Float = 0f
  var sliceSize:Float = 0f
  // Bottom, top
  var xAxesStart:(Float,Float) = (0f, 0f)
  // Left, right
  var yAxesStart:(Float,Float) = (0f, 0f)
  var slicesStartX:Float = 0f
  var slicesStartY:Float = 0f
  // Left, right
  var colorbarStartX:(Float,Float) = (0f, 0f)
  var colorbarStartY:Float = 0f
  */

  def sortedDims : List[String] = project.currentZoom.dimNames.sorted

  def plotData(model:GpModel,
               d1:(String,(Float,Float)), 
               d2:(String,(Float,Float)), 
               slice:Map[String,Float]) : Matrix2D = {
    model.sampleSlice(d1, d2, slice.toList)._1._2
  }

  def draw = {
    applet.background(Config.backgroundColor)

    // Compute the spacing of everything
    val startTime = System.currentTimeMillis
    val maxResponseWidth = width -
      ((project.currentZoom.length-1) * Config.plotSpacing) -
      (Config.axisSize * 2) -
      (Config.plotSpacing * 2) -
      (Config.colorbarSpacing * 4) -
      (Config.colorbarWidth * 2)
    val maxResponseHeight = height - 
      ((project.currentZoom.length-1) * Config.plotSpacing) -
      (Config.axisSize * 2) -
      (Config.plotSpacing * 2)
    val responseSize = math.min(maxResponseWidth, maxResponseHeight)
    val sliceSize = responseSize / project.currentZoom.length - 
                      Config.plotSpacing

    // Bottom, top
    val xAxesStart:(Float,Float) = 
      (Config.plotSpacing + Config.axisSize + 
          responseSize - Config.plotSpacing,
       Config.plotSpacing)
    // Left, right
    val yAxesStart:(Float,Float) = {
      val colorbarOffset = Config.colorbarSpacing * 2 + Config.colorbarWidth
      (colorbarOffset + Config.plotSpacing, 
       colorbarOffset + Config.plotSpacing + Config.axisSize + 
         responseSize - Config.plotSpacing)
    }
    val slicesStartX = yAxesStart._1 + Config.axisSize
    val slicesStartY = xAxesStart._2 + Config.axisSize
    
    // left, right
    val colorbarStartX = 
      (Config.colorbarSpacing, 
       yAxesStart._2 + Config.axisSize + 
         Config.plotSpacing + Config.colorbarSpacing)
    val colorbarStartY = slicesStartY

    def drawResp1(xf:String, yf:String, 
                  x:Float, y:Float) = {
      drawResponse(resp1Info, xf, yf, x, y, 
                   sliceSize, xAxesStart._1, yAxesStart._1,
                   colorbarStartX._1, colorbarStartY,
                   responseSize)
    }
    def drawResp2(xf:String, yf:String, 
                  x:Float, y:Float) = {
      drawResponse(resp2Info, xf, yf, x, y, 
                   sliceSize, xAxesStart._2, yAxesStart._2,
                   colorbarStartX._2, colorbarStartY,
                   responseSize)
    }

    // Draw the splom itself
    sortedDims.foldLeft(slicesStartX) {case (xPos, xFld) =>
      sortedDims.foldLeft(slicesStartY) {case (yPos, yFld) =>
        if(xFld < yFld) {
          // response1 goes in the lower left
          drawResp1(xFld, yFld, xPos, yPos)
        } else if(xFld > yFld) {
          // response2 goes in the upper right
          // x and y field names here are actually reversed
          drawResp2(yFld, xFld, xPos, yPos)
        }
        yPos + sliceSize + Config.plotSpacing
      }
      xPos + sliceSize + Config.plotSpacing
    }

    val endTime = System.currentTimeMillis
    //println("draw time: " + (endTime - startTime) + "ms")
  }

  private def drawResponse(responseInfo:Option[ResponseInfo], 
                           xFld:String, yFld:String, 
                           xPos:Float, yPos:Float, sliceSize:Float,
                           xAxisStart:Float, yAxisStart:Float,
                           colorbarStartX:Float, colorbarStartY:Float,
                           colorbarHeight:Float) = {
    val xRange = (xFld, project.currentZoom.range(xFld))
    val yRange = (yFld, project.currentZoom.range(yFld))
    responseInfo foreach {case (field, model, xAxes, yAxes, legend, plots) =>
      // Drawing the legends is easy
      legend.draw(this, colorbarStartX, colorbarStartY, 
                        Config.colorbarWidth, colorbarHeight)
      val data = plotData(model, xRange, yRange, project.currentSlice)
      val plot = plots((xFld, yFld))
      plot.draw(this, xPos, yPos, sliceSize, sliceSize, data)
      // See if we should draw the axes
      if(yFld == sortedDims.last) {
        //println(xFld + ": " + xPos + " " + xAxisStart)
        xAxes(xFld).draw(this, xPos, xAxisStart,
                         sliceSize, Config.axisSize, 
                         xRange)
      }
      if(xFld == sortedDims.head) {
        yAxes(yFld).draw(this, yAxisStart, yPos,
                         Config.axisSize, sliceSize, 
                         yRange)
      }
    }
  }

  private def createPlots(cm:SpecifiedColorMap) : PlotInfoMap = {
    project.inputFields.flatMap({fld1 =>
      project.inputFields.flatMap({fld2 =>
        if(fld1 < fld2) {
          Some(((fld1, fld2), 
            new ContinuousPlot(project.currentZoom.min(fld1), 
                               project.currentZoom.max(fld1),
                               project.currentZoom.min(fld2), 
                               project.currentZoom.max(fld2),
                               cm)))
        } else {
          None
        }
      })
    }).toMap
  }
  
  private def createAxes(position:Axis.Placement) = {
    val fields = position match {
      case Axis.HorizontalTop | Axis.HorizontalBottom => 
        sortedDims.init
      case Axis.VerticalLeft | Axis.VerticalRight => 
        sortedDims.tail
    }
    fields.map {fld => (fld, new Axis(position))} toMap
  }

  override def mouseDragged(prevMouseX:Int, prevMouseY:Int, 
                            mouseX:Int, mouseY:Int,
                            button:P5Panel.MouseButton.Value) = {
    // Now figure out if we need to deal with any mouse 
    // movements in the colorbars
    if(mouseButton == P5Panel.MouseButton.Left) {
      //val (mouseX, mouseY) = mousePos
      resp1Info.foreach {case (_, _, _, _, cb, plots) =>
        if(cb.isInside(mouseX, mouseY)) {
          handleBarMouse(mouseX, mouseY, cb)
        }
      }
      resp2Info.foreach {case (_, _, _, _, cb, plots) =>
        if(cb.isInside(mouseX, mouseY)) {
          handleBarMouse(mouseX, mouseY, cb)
        }
      }
    }
  }

  override def mousePressed(mouseX:Int, mouseY:Int, 
                            button:P5Panel.MouseButton.Value) = {
    // Now figure out if we need to deal with any mouse 
    // movements in the colorbars
    if(button == P5Panel.MouseButton.Left) {
      //val (mouseX, mouseY) = mousePos
      resp1Info.foreach {case (_, _, _, _, cb, plots) =>
        if(cb.isInside(mouseX, mouseY)) {
          handleBarMouse(mouseX, mouseY, cb)
        }
      }
      resp2Info.foreach {case (_, _, _, _, cb, plots) =>
        if(cb.isInside(mouseX, mouseY)) {
          handleBarMouse(mouseX, mouseY, cb)
        }
      }
    }
  }

  def handleBarMouse(mouseX:Int, mouseY:Int, cb:Colorbar) = {
    val bar = cb.barBounds
    if(bar.isInside(mouseX, mouseY)) {
      val cm = cb.colormap
      val filterVal = P5Panel.map(mouseY, bar.minY, bar.maxY, 
                                          cm.minVal, cm.maxVal)
      cm.filterVal = filterVal
    }
  }
}

