package tuner.gui

import tuner.Config
import tuner.Table
import tuner.geom.Rectangle
import tuner.gui.util.AxisTicks
import tuner.gui.util.FacetLayout
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Scatterplot
import tuner.project.Sampler

/**
 * A SPLOM showing the locations of sample points taken
 */
class SamplerSplomPanel(project:Sampler)
  extends P5Panel(Config.samplerSplomDims._1, 
                  Config.samplerSplomDims._2, 
                  P5Panel.Java2D) {
  
  // which output value we're showing
  var _selectedResponse:Option[String] = None

  // what to draw
  var drawSamples:Table = project.newSamples
  
  var splomBounds = Rectangle((0f,0f), (0f,0f))
  val sploms = inputFields.flatMap({fld1 =>
    inputFields.flatMap({fld2 =>
      if(fld1 < fld2) {
        Some(((fld1, fld2), new Scatterplot(Config.sampleDotColor)))
      } else {
        None
      }
    })
  }).toMap
  val xAxes:Map[String,Axis] = 
    inputFields.foldLeft(Map[String,Axis]()) {case (xa, fld) =>
      xa + (fld -> new Axis(Axis.HorizontalBottom))
    }
  val yAxes:Map[String,Axis] = 
    inputFields.foldLeft(Map[String,Axis]()) {case (ya,fld) =>
      ya + (fld -> new Axis(Axis.VerticalLeft))
    }

  def inputFields = project.sampleRanges.dimNames.sorted

  /**
   * The name of the currently selected response
   */
  def selectedResponse = _selectedResponse
  def selectedResponse_=(r:Option[String]) = {
    _selectedResponse = r
    redraw
  }

  override def setup = {
    loop = false
  }
  
  /**
   * Force a redraw
   */
  def redraw = {
    loop = true
  }

  /**
   * Do all the drawing required.  Doesn't loop by default as 
   * this isn't interactive
   */
  def draw = {
    loop = false

    applet.background(Config.backgroundColor)

    // Make sure we have something to draw
    if(drawSamples.numRows > 0) {
      // Compute all the sizes of things
      val totalSize = math.min(width, height) - 
                      Config.plotSpacing * 2 - 
                      Config.axisSize
      splomBounds = Rectangle((Config.plotSpacing+Config.axisSize, 
                               Config.plotSpacing), 
                              totalSize, totalSize)
      val (_, plotBounds) = 
        FacetLayout.plotBounds(splomBounds, inputFields)
      // Draw all the sploms
      inputFields.foreach {xFld =>
        inputFields.foreach {yFld =>
          if(xFld < yFld) {
            drawPlot(plotBounds((xFld, yFld)), xFld, yFld)
          }
        }
      }

  private def drawPlot(bounds:Rectangle, xFld:String, yFld:String) = {
    val plot = sploms((xFld, yFld))
    val (minX,maxX) = project.sampleRanges.range(xFld)
    val (minY,maxY) = project.sampleRanges.range(yFld)
    val xTicks = AxisTicks.ticks(minX, maxX, 
                                 bounds.width, Config.smallFontSize)
    val yTicks = AxisTicks.ticks(minY, maxY,
                                 bounds.height, Config.smallFontSize)

    // Draw a nice white background
    fill(255)
    rectMode(P5Panel.RectMode.Corner)
    rect(bounds.minX, bounds.minY, bounds.width, bounds.height)

    // Draw the actual plot
    val (dataXMin, dataXMax) = if(xTicks.isEmpty) {
      (minX, maxX)
    } else {
      (xTicks.min, xTicks.max)
    }
    val (dataYMin, dataYMax) = if(yTicks.isEmpty) {
      (minY, maxY)
    } else {
      (yTicks.min, yTicks.max)
    }
    plot.draw(this, bounds.minX, bounds.minY, bounds.width, bounds.height,
              drawSamples,
              (xFld, (dataXMin, dataXMax)),
              (yFld, (dataYMin, dataYMax)))
    if(xFld != inputFields.last && !xTicks.isEmpty) {
      xAxes(xFld).draw(this, bounds.minX, splomBounds.maxY, 
                             bounds.width, Config.axisSize,
                             xFld, xTicks)
    }
    if(yFld != inputFields.head && !yTicks.isEmpty) {
      yAxes(yFld).draw(this, Config.plotSpacing, bounds.minY, 
                             Config.axisSize, bounds.height, 
                             yFld, yTicks)
    }
  }
}

