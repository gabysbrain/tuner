package tuner.gui.widgets

import tuner.Config
import tuner.gui.P5Panel

object Widgets {
  
  def crosshair(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
                xSlice:Float, ySlice:Float,
                xRange:(Float,Float), yRange:(Float,Float)) = {

    applet.pushMatrix
    applet.translate(x, y)

    val xx = P5Panel.map(xSlice, xRange._1, xRange._2, 0, w)
    val yy = P5Panel.map(ySlice, yRange._2, yRange._1, 0, h)

    applet.stroke(Config.crosshairColor)
    applet.line(xx-Config.crosshairRadius, yy, xx+Config.crosshairRadius, yy)
    applet.line(xx, yy-Config.crosshairRadius, xx, yy+Config.crosshairRadius)

    applet.popMatrix
  }

  def sampleLine(applet:P5Panel, x:Float, y:Float, w:Float, h:Float,
                 xSlice:Float, ySlice:Float, xSample:Float, ySample:Float,
                 xRange:(Float,Float), yRange:(Float,Float)) = {

    applet.pushMatrix
    applet.translate(x, y)

    val xx1 = P5Panel.map(xSlice, xRange._1, xRange._2, 0, w)
    val yy1 = P5Panel.map(ySlice, yRange._2, yRange._1, 0, h)
    val xx2 = P5Panel.map(xSample, xRange._1, xRange._2, 0, w)
    val yy2 = P5Panel.map(ySample, yRange._2, yRange._1, 0, h)

    applet.stroke(Config.sampleLineColor)
    applet.fill(Config.sampleLineColor)
    applet.line(xx1, yy1, xx2, yy2)
    applet.strokeWeight(Config.sampleLineWidth)

    applet.ellipseMode(P5Panel.EllipseMode.Center)
    val radius = Config.sampleLineDotRadius
    applet.ellipse(xx2, yy2, radius, radius)

    applet.popMatrix
  }
  
}

