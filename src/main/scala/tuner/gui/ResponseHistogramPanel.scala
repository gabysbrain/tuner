package tuner.gui

import tuner.Config
import tuner.GpModel
import tuner.Project
import tuner.Sampler
import tuner.Table
import tuner.geom.Rectangle
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Histogram

class ResponseHistogramPanel(project:Project, responseField:String) 
    extends P5Panel(Config.respHistogramPanelDims._1, 
                    Config.respHistogramPanelDims._2, 
                    P5Panel.Java2D) {

  var responses:Option[Table] = None
  var histogram:Option[Histogram] = None
  var minResponse = Float.MinValue
  var maxResponse = Float.MaxValue
  val xAxis = new Axis(Axis.HorizontalBottom)
  val yAxis = new Axis(Axis.VerticalLeft)

  var yAxisBounds = Rectangle((0f,0f),(0f,0f))
  var xAxisBounds = Rectangle((0f,0f),(0f,0f))
  var plotBounds = Rectangle((0f,0f),(0f,0f))
  var sliderBounds = Rectangle((0f,0f),(0f,0f))

  override def setup = {
    val (resp, minVal, maxVal) = {
      val samples = Sampler.lhc(project.inputs, 
                                Config.respHistogramSampleDensity)
      val models = project.gpModels.get
      val model = models(responseField)
      (model.sampleTable(samples), model.funcMin, model.funcMax)
    }

    val hist = new Histogram(Config.respHistogramBarStroke, 
                             Config.respHistogramBarFill, 
                             minVal, maxVal, 
                             Config.respHistogramBars)
    responses = Some(resp)
    minResponse = resp.min(responseField)
    maxResponse = resp.max(responseField)
    histogram = Some(hist)
  }

  def draw = {
    val startTime = System.currentTimeMillis

    val plotWidth = width - (Config.plotSpacing * 2) - Config.axisSize
    val plotHeight = height - (Config.plotSpacing * 2) - 
                              Config.axisSize - 
                              Config.respHistogramHandleSize._2
    yAxisBounds = Rectangle((Config.plotSpacing, Config.plotSpacing),
                            Config.axisSize, plotHeight)
    plotBounds = Rectangle((yAxisBounds.maxX, Config.plotSpacing),
                           plotWidth, plotHeight)
    xAxisBounds = Rectangle((plotBounds.minX, plotBounds.maxY),
                            plotWidth, Config.axisSize)
    sliderBounds = Rectangle((xAxisBounds.minX, xAxisBounds.maxY),
                             plotWidth, Config.respHistogramHandleSize._2)

    applet.background(Config.backgroundColor)
    (histogram, responses) match {
      case (Some(h), Some(r)) =>
        h.draw(this, plotBounds.minX, plotBounds.minY, 
                     plotBounds.width, plotBounds.height, 
                     responseField, r)
        // Figure out how to draw the y axis
        val maxCount = h.counts.values.max
        yAxis.draw(this, yAxisBounds.minX, yAxisBounds.minY,
                         yAxisBounds.width, yAxisBounds.height,
                         ("Count", (0, maxCount)))
        val xTicks = minResponse +: h.breaks :+ maxResponse
        xAxis.draw(this, xAxisBounds.minX, xAxisBounds.minY,
                         xAxisBounds.width, xAxisBounds.height,
                         responseField, xTicks)
      case _ =>
    }
    val endTime = System.currentTimeMillis
    //println("hist draw time: " + (endTime - startTime) + "ms")
  }
}

