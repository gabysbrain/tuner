package tuner.gui

import tuner.Config
import tuner.Matrix2D
import tuner.Sampler
import tuner.SpecifiedColorMap
import tuner.Table
import tuner.geom.Rectangle
import tuner.gui.event.CandidateChanged
import tuner.gui.util.Histogram
import tuner.gui.widgets.Axis
import tuner.gui.widgets.ContinuousPlot
import tuner.gui.widgets.Bars
import tuner.gui.widgets.Scatterplot
import tuner.project.Viewable
import tuner.util.AxisTicks

class ParetoPanel(project:Viewable)
    extends P5Panel(Config.paretoDims._1, 
                    Config.paretoDims._2, 
                    P5Panel.Java2D) {

  val models = project.gpModels

  val xAxis = new Axis(Axis.HorizontalBottom)
  val yAxis = new Axis(Axis.VerticalLeft)
  val activeSampleScatterplot = new Scatterplot(true)
  val inactiveSampleScatterplot = 
    new Scatterplot(false)
  val histogram = new Bars(Config.respHistogramBarStroke,
                           Config.respHistogramBarFill)
  //val csp = new ContinuousPlot

  var xAxisBox = Rectangle((0f,0f), (0f,0f))
  var yAxisBox = Rectangle((0f,0f), (0f,0f))
  var plotBox = Rectangle((0f,0f), (0f,0f))

  // Caching for the samples stuff
  var pareto1dField = ""
  var pareto1dCounts:Map[Float,Int] = Map[Float,Int]()
  var pareto2dFields = ("", "")
  //var pareto2dData:Matrix2D = null
  //var cspColorMap:SpecifiedColorMap = null
  var xAxisTicks:List[Float] = Nil
  var xAxisRange:(Float,Float) = (0f,0f)
  var yAxisTicks:List[Float] = Nil
  var yAxisRange:(Float,Float) = (0f,0f)

  override def setup = {
    frameRate = 30
  }

  def draw = {
    applet.background(Config.backgroundColor)

    val plotStartX = (Config.plotSpacing + Config.axisSize).toFloat
    val plotStartY = Config.plotSpacing.toFloat
    val plotWidth = (width - Config.plotSpacing * 2 -
                     Config.axisSize).toFloat
    val plotHeight = (height - Config.plotSpacing * 2 -
                      Config.axisSize).toFloat

    xAxisBox = Rectangle((plotStartX, plotStartY+plotHeight), 
                         plotWidth, Config.axisSize)
    yAxisBox = Rectangle((Config.plotSpacing.toFloat, plotStartY), 
                         Config.axisSize, plotHeight)
    plotBox = Rectangle((plotStartX, plotStartY), plotWidth, plotHeight)

    (project.viewInfo.response1View, project.viewInfo.response2View) match {
      case (Some(r1), Some(r2)) => draw2dPareto(r1, r2)
      case (Some(r1), None) => draw1dPareto(r1)
      case (None, Some(r2)) => draw1dPareto(r2)
      case (None, None) => // Draw nothing
    }

  }

  def draw1dPareto(resp:String) {
    val data = project.modelSamples
    if(data.fieldNames.contains(resp)) {
      val model = models(resp)
      val ticks = AxisTicks.ticks(model.funcMin, model.funcMax, 
                                  xAxisBox.width, Config.smallFontSize)
      xAxis.draw(this, xAxisBox.minX, xAxisBox.minY,
                       xAxisBox.width, xAxisBox.height,
                       resp, ticks)
      if(resp != pareto1dField) {
        pareto1dField = resp
        pareto1dCounts = Histogram.countData(resp, data, Config.respHistogramBars)
      }
      val (yMin, yMax) = (pareto1dCounts.values.min, pareto1dCounts.values.max)
      histogram.draw(this, plotBox.minX, plotBox.minY, 
                           plotBox.width, plotBox.height,
                           pareto1dCounts.values.map(_.toFloat).toList,
                           yMin, yMax)
    }
  }

  def draw2dPareto(resp1:String, resp2:String) {
    val r1Model = models(resp1)
    val r2Model = models(resp2)

    val (xt, xr) = AxisTicks.ticksAndRange(
      r1Model.funcMin, r1Model.funcMax,
      xAxisBox.width, Config.smallFontSize)
    xAxisTicks = xt
    xAxisRange = xr
    val (yt, yr) = AxisTicks.ticksAndRange(
        r2Model.funcMin, r2Model.funcMax,
        yAxisBox.height, Config.smallFontSize)
    yAxisTicks = yt
    yAxisRange = yr

      /*
    if((resp1, resp2) != pareto2dFields) {
      pareto2dFields = (resp1, resp2)
      val samp = project.randomSample2dResponse(r2Range, r1Range)
      // Put the counts on a log scale
      //pareto2dData = samp.map(x => if(x==0) x else math.log(x).toFloat)
      cspColorMap = new SpecifiedColorMap(tuner.RedColorMap, 
                                          pareto2dData.min, 
                                          pareto2dData.max)
    }
      */

    xAxis.draw(this, xAxisBox.minX, xAxisBox.minY, 
                     xAxisBox.width, xAxisBox.height,
                     resp1, xAxisTicks)
    yAxis.draw(this, yAxisBox.minX, yAxisBox.minY, 
                     yAxisBox.width, yAxisBox.height,
                     resp2, yAxisTicks)

    // Now for the csp
    /*
    csp.draw(this, plotBox.minX, plotBox.minY, 
                   plotBox.width, plotBox.height,
                   pareto2dData, 0, 0, 
                   r1Range._2, r2Range._2,
                   cspColorMap)
    */

    // Draw the scatterplot over the csp
    rectMode(P5Panel.RectMode.Corner)
    fill(255)
    rect(plotBox.minX, plotBox.minY, plotBox.width, plotBox.height)

    val (activeDesign, inactiveDesign) = project.viewFilterDesignSites
    // Draw the inactive samples first so they're behind
    inactiveSampleScatterplot.draw(this, plotBox.minX, plotBox.minY, 
                                         plotBox.width, plotBox.height, 
                                         inactiveDesign, 
                                         (resp1, xAxisRange), 
                                         (resp2, yAxisRange),
                            (_:Table.Tuple) => Config.paretoInactiveSampleColor)
    activeSampleScatterplot.draw(this, plotBox.minX, plotBox.minY, 
                                       plotBox.width, plotBox.height, 
                                       activeDesign, 
                                       (resp1, xAxisRange), 
                                       (resp2, yAxisRange),
                                    (_:Table.Tuple) => Config.paretoSampleColor)
  }

  override def mouseClicked(mouseX:Int, mouseY:Int, 
                            button:P5Panel.MouseButton.Value) = {
    
    (project.viewInfo.response1View, project.viewInfo.response2View) match {
      case (Some(r1), Some(r2)) =>
        mouseClick2d(mouseX, mouseY, button, r1, r2)
      case _ =>
    }
  }

  def mouseClick2d(mouseX:Int, mouseY:Int, button:P5Panel.MouseButton.Value,
                   response1:String, response2:String) = {

    val data = project.designSites
    val r1Model = models(response1)
    val r2Model = models(response2)

    val (minX, maxX) = xAxisRange
    val (minY, maxY) = yAxisRange

    // See if we hit upon any sample points
    var minDist = Float.MaxValue
    var minInfo:(Float,Float) = (-1f, -1f)
    var tmp:(Float,Float) = (-1f, -1f)
    for(r <- 0 until data.numRows) {
      val tpl = data.tuple(r)
      if(project.viewInfo.inView(tpl.toList)) {
        val (dataX, dataY) = (tpl(response1), tpl(response2))
        val xx = P5Panel.map(dataX, minX, maxX, plotBox.minX, plotBox.maxX)
        val yy = P5Panel.map(dataY, maxY, minY, plotBox.minY, plotBox.maxY)
        val dist = P5Panel.dist(mouseX, mouseY, xx, yy)
  
        if(dist < minDist) {
          minDist = dist
          minInfo = (dataX, dataY)
          tmp = (xx, yy)
        }
      }
    }
    //println("md: " + minDist + " " + tmp + " " + Config.scatterplotDotSize)
    //println("mp: " + minInfo)
    // Figure out if we're inside a point
    if(minDist < Config.scatterplotDotSize*2) {
      publish(new CandidateChanged(this, 
        List((response1, minInfo._1), (response2, minInfo._2))))
    }
  }
}

