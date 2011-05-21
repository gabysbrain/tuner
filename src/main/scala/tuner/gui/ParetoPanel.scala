package tuner.gui

import tuner.Config
import tuner.Project
import tuner.geom.Rectangle
import tuner.gui.widgets.Axis

class ParetoPanel(project:Project)
    extends P5Panel(Config.paretoDims._1, Config.paretoDims._2, P5Panel.OpenGL) {

  val models = project.gpModels.get

  val xAxis = new Axis(Axis.HorizontalBottom)
  val yAxis = new Axis(Axis.VerticalLeft)

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

    (project.response1View, project.response2View) match {
      case (Some(r1), Some(r2)) => draw2dPareto(r1, r2)
      case (Some(r1), None) => draw1dPareto(r1)
      case (None, Some(r2)) => draw1dPareto(r2)
      case (None, None) => // Draw nothing
    }

    fill(255)
    rectMode(P5Panel.RectMode.Corner)
    rect(plotStartX, plotStartY, plotWidth, plotHeight)
    //rect(xAxisBox.minX, xAxisBox.minY, xAxisBox.width, xAxisBox.height)
  }

  def draw1dPareto(resp:String) {
  }

  def draw2dPareto(resp1:String, resp2:String) {
    val r1Model = models(resp1)
    val r2Model = models(resp2)
    xAxis.draw(this, xAxisBox.minX, xAxisBox.minY, 
                     xAxisBox.width, xAxisBox.height,
                     (resp1, (r1Model.funcMin, r1Model.funcMax)))
    yAxis.draw(this, yAxisBox.minX, yAxisBox.minY, 
                     yAxisBox.width, yAxisBox.height,
                     (resp2, (r2Model.funcMin, r2Model.funcMax)))
  }

}

