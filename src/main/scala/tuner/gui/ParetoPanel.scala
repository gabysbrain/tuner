package tuner.gui

import tuner.Config
import tuner.Project
import tuner.geom.Rectangle
import tuner.gui.event.CandidateChanged
import tuner.gui.util.AxisTicks
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Scatterplot

class ParetoPanel(project:Project)
    extends P5Panel(Config.paretoDims._1, Config.paretoDims._2, P5Panel.Java2D) {

  val models = project.gpModels.get

  val xAxis = new Axis(Axis.HorizontalBottom)
  val yAxis = new Axis(Axis.VerticalLeft)
  val sampleScatterplot = new Scatterplot(Config.paretoSampleColor)

  var xAxisBox = Rectangle((0f,0f), (0f,0f))
  var yAxisBox = Rectangle((0f,0f), (0f,0f))
  var plotBox = Rectangle((0f,0f), (0f,0f))

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

    fill(255)
    rectMode(P5Panel.RectMode.Corner)
    rect(plotBox.minX, plotBox.minY, plotBox.width, plotBox.height)

    (project.viewInfo.response1View, project.viewInfo.response2View) match {
      case (Some(r1), Some(r2)) => draw2dPareto(r1, r2)
      case (Some(r1), None) => draw1dPareto(r1)
      case (None, Some(r2)) => draw1dPareto(r2)
      case (None, None) => // Draw nothing
    }

  }

  def draw1dPareto(resp:String) {
    val model = models(resp)
    val ticks = AxisTicks.ticks(model.funcMin, model.funcMax)
    xAxis.draw(this, xAxisBox.minX, xAxisBox.minY,
                     xAxisBox.width, xAxisBox.height,
                     resp, ticks)
  }

  def draw2dPareto(resp1:String, resp2:String) {
    val r1Model = models(resp1)
    val r2Model = models(resp2)
    val r1Ticks = AxisTicks.ticks(r1Model.funcMin, r1Model.funcMax)
    val r2Ticks = AxisTicks.ticks(r2Model.funcMin, r2Model.funcMax)
    xAxis.draw(this, xAxisBox.minX, xAxisBox.minY, 
                     xAxisBox.width, xAxisBox.height,
                     resp1, r1Ticks)
    yAxis.draw(this, yAxisBox.minX, yAxisBox.minY, 
                     yAxisBox.width, yAxisBox.height,
                     resp2, r2Ticks)
    sampleScatterplot.draw(this, plotBox.minX, plotBox.minY, 
                                 plotBox.width, plotBox.height, 
                                 project.designSites.get, 
                                 (resp1, (r1Ticks.min,r1Ticks.max)),
                                 (resp2, (r2Ticks.min,r2Ticks.max)))
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

    val data = project.designSites.get
    val (minX, maxX) = (data.min(response1), data.max(response1))
    val (minY, maxY) = (data.min(response2), data.max(response2))

    // See if we hit upon any sample points
    for(r <- 0 until data.numRows) {
      val tpl = data.tuple(r)
      val (dataX, dataY) = (tpl(response1), tpl(response2))
      val xx = P5Panel.map(dataX, minX, maxX, plotBox.minX, plotBox.maxX)
      val yy = P5Panel.map(dataY, minY, maxY, plotBox.maxY, plotBox.minY)
      val dist = P5Panel.dist(mouseX, mouseY, xx, yy)
      if(dist < Config.scatterplotDotSize) {
        publish(new CandidateChanged(this, 
          List((response1, dataX), (response2, dataY))))
      }
    }
  }
}

