package tuner.gui

import tuner.Config
import tuner.GpModel
import tuner.Project
import tuner.Sampler
import tuner.Table
import tuner.geom.Rectangle
import tuner.gui.event.ReadyToDraw
import tuner.gui.util.AxisTicks
import tuner.gui.util.Histogram
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Bars

class ResponseHistogramPanel(project:Project, responseField:String) 
    extends P5Panel(Config.respHistogramPanelDims._1, 
                    Config.respHistogramPanelDims._2, 
                    P5Panel.Java2D) {

  val histogram = new Bars(Config.respHistogramBarStroke, 
                           Config.respHistogramBarFill)
  var counts:Map[Float,Float] = Map()
  var xAxisTicks:List[Float] = Nil
  var yAxisTicks:List[Float] = List(0,0.5f,1f)
  val xAxis = new Axis(Axis.HorizontalBottom)
  val yAxis = new Axis(Axis.VerticalLeft)

  var yAxisBounds = Rectangle((0f,0f),(0f,0f))
  var xAxisBounds = Rectangle((0f,0f),(0f,0f))
  var plotBounds = Rectangle((0f,0f),(0f,0f))
  var sliderBounds = Rectangle((0f,0f),(0f,0f))

  override def setup = {
    val data = project.modelSamples

    /*
    val breaks = Histogram.computeBreaks(data.min(responseField), 
                                         data.max(responseField), 
                                         Config.respHistogramBars)
    */
    counts = Histogram.pctData(responseField, data, Config.respHistogramBars)
    //xAxisTicks = breaks.window(2)
    xAxisTicks = counts.keys.toList
    val minY = counts.values.min
    val maxY = counts.values.max
    yAxisTicks = List(minY, (minY+maxY)/2, maxY)
    publish(new ReadyToDraw(this))
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
    sliderBounds = Rectangle((xAxisBounds.minX, xAxisBounds.minY),
                             plotWidth, Config.respHistogramHandleSize._2)

    applet.background(Config.backgroundColor)
    histogram.draw(this, plotBounds.minX, plotBounds.minY, 
                   plotBounds.width, plotBounds.height, 
                   counts.values.map(_.toFloat).toList,
                   yAxisTicks.min, yAxisTicks.max)
        // Figure out how to draw the y axis
    //val maxCount = counts.values.max
    yAxis.draw(this, yAxisBounds.minX, yAxisBounds.minY,
                     yAxisBounds.width, yAxisBounds.height,
                     "Pct", yAxisTicks)
    //val xTicks = minResponse +: h.breaks :+ maxResponse
    xAxis.draw(this, xAxisBounds.minX, xAxisBounds.minY,
                     xAxisBounds.width, xAxisBounds.height,
                     responseField, xAxisTicks)
    drawSlider
    val endTime = System.currentTimeMillis
    //println("hist draw time: " + (endTime - startTime) + "ms")
  }

  def drawSlider = {
    val models = project.gpModels.get
    val model = models(responseField)
    val (est, _) = model.runSample(project.viewInfo.currentSlice.toList)
    val xx = P5Panel.map(est.toFloat, xAxisTicks.min, xAxisTicks.max, 
                              sliderBounds.minX, sliderBounds.maxX)

    noStroke
    fill(Config.respHistogramBarFill.get)
    triangle(xx, sliderBounds.minY, 
             xx + (Config.respHistogramHandleSize._1/2), sliderBounds.maxY,
             xx - (Config.respHistogramHandleSize._1/2), sliderBounds.maxY)
  }
}

