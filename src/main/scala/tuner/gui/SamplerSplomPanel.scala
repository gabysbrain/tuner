package tuner.gui

import tuner.Config
import tuner.SpecifiedColorMap
import tuner.Table
import tuner.geom.Rectangle
import tuner.gui.util.FacetLayout
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Colorbar
import tuner.gui.widgets.Scatterplot
import tuner.project.Sampler
import tuner.util.AxisTicks

/**
 * A SPLOM showing the locations of sample points taken
 */
class SamplerSplomPanel(project:Sampler)
  extends P5Panel(Config.samplerSplomDims._1, 
                  Config.samplerSplomDims._2, 
                  P5Panel.Java2D) {
  
  // which output value we're showing
  private var _selectedResponse:Option[String] = None

  // what to draw
  var drawSamples:Table = project.newSamples
  
  var splomBounds = Rectangle((0f,0f), (0f,0f))
  val sploms = inputFields.flatMap({fld1 =>
    inputFields.flatMap({fld2 =>
      if(fld1 < fld2) {
        Some(((fld1, fld2), new Scatterplot))
      } else {
        None
      }
    })
  }).toMap
  val legend = new Colorbar(Colorbar.Right, false)
  // This will get overridden when the user changes the response variable
  var colormap:Option[SpecifiedColorMap] = None
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
    colormap = r.map {resp =>
      new SpecifiedColorMap(Config.sampleColorMap, 
                            drawSamples.min(resp),
                            drawSamples.max(resp),
                            false)
    }
  }

  override def setup = {
    super.setup
    loop = false
  }
  
  /**
   * Force a redraw
   */
  def redraw = applet.loop

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

      // Draw the color bar if we're coloring the points
      selectedResponse.foreach {respFld => drawLegend(splomBounds, respFld)}
    }
  }

  /**
   * Draw a single scatterplot panel
   */
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
    // May need to draw colored dots
    val colorFun:(Table.Tuple=>Int) = colormap match {
      case Some(cm) => tpl => cm.color(tpl(selectedResponse.get))
      case None     => _ => Config.sampleDotColor
    }
    plot.draw(this, bounds.minX, bounds.minY, bounds.width, bounds.height,
              drawSamples,
              (xFld, (dataXMin, dataXMax)),
              (yFld, (dataYMin, dataYMax)),
              colorFun)
    if(xFld != inputFields.last && !xTicks.isEmpty) {
      xAxes(xFld).draw(this, bounds.minX, splomBounds.maxY, 
                             bounds.width, Config.axisSize,
                             xFld, minX, maxX)
    }
    if(yFld != inputFields.head && !yTicks.isEmpty) {
      yAxes(yFld).draw(this, Config.plotSpacing, bounds.minY, 
                             Config.axisSize, bounds.height, 
                             yFld, minY, maxY)
    }
  }

  /**
   * Draw the colormap legend
   */
  def drawLegend(bounds:Rectangle, response:String) = colormap.foreach {cm =>
    val legendBounds = Rectangle(
      (bounds.maxX-Config.colorbarWidth, bounds.minY),
      Config.colorbarWidth, bounds.height)
    legend.draw(this, legendBounds.minX, legendBounds.minY, 
                      legendBounds.width, legendBounds.height, 
                      response, cm)
  }
}

