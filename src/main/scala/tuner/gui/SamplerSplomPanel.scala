package tuner.gui

import tuner.Config
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

  override def setup = {
    loop = false
  }
  
  def redraw = {
    loop = true
  }

  def draw = {
    loop = false

    val ranges = project.sampleRanges

    applet.background(Config.backgroundColor)

    // Make sure we have something to draw
    if(project.newSamples.numRows > 0) {
      // Compute all the sizes of things
      val totalSize = math.min(width, height) - 
                      Config.plotSpacing * 2 - 
                      Config.axisSize
      splomBounds = Rectangle((Config.plotSpacing+Config.axisSize, 
                               Config.plotSpacing), 
                              totalSize, totalSize)
      val (_, plotBounds) = 
        FacetLayout.plotBounds(splomBounds, inputFields)
      inputFields.foreach {xFld =>
        inputFields.foreach {yFld =>
          if(xFld < yFld) {
            val bound = plotBounds((xFld, yFld))
            val plot = sploms((xFld, yFld))
            val (minX,maxX) = ranges.range(xFld)
            val (minY,maxY) = ranges.range(yFld)
            val xTicks = AxisTicks.ticks(minX, maxX, 
                                         bound.width, Config.smallFontSize)
            val yTicks = AxisTicks.ticks(minY, maxY,
                                         bound.height, Config.smallFontSize)

            // Draw a nice white background
            fill(255)
            rectMode(P5Panel.RectMode.Corner)
            rect(bound.minX, bound.minY, bound.width, bound.height)

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
            plot.draw(this, bound.minX, bound.minY, bound.width, bound.height,
                      project.newSamples,
                      (xFld, (dataXMin, dataXMax)),
                      (yFld, (dataYMin, dataYMax)))
            if(xFld != inputFields.last && !xTicks.isEmpty) {
              xAxes(xFld).draw(this, bound.minX, splomBounds.maxY, 
                                     bound.width, Config.axisSize,
                                     xFld, xTicks)
            }
            if(yFld != inputFields.head && !yTicks.isEmpty) {
              yAxes(yFld).draw(this, Config.plotSpacing, bound.minY, 
                                     Config.axisSize, bound.height, 
                                     yFld, yTicks)
            }
          }
        }
      }
    }
  }
}

