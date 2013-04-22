package tuner.gui.widgets

import tuner.Color
import tuner.Config
import tuner.gui.P5Panel

import java.awt.Graphics2D

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

  def crosshair(g:Graphics2D, x:Float, y:Float, w:Float, h:Float, 
                xSlice:Float, ySlice:Float,
                xRange:(Float,Float), yRange:(Float,Float)) = {

    // Get the screen coords within the slice
    val xx = x + P5Panel.map(xSlice, xRange._1, xRange._2, 0, w) toInt
    val yy = y + P5Panel.map(ySlice, yRange._2, yRange._1, 0, h) toInt

    g.setPaint(Config.crosshairColor)
    g.drawLine(xx-Config.crosshairRadius, yy, xx+Config.crosshairRadius, yy)
    g.drawLine(xx, yy-Config.crosshairRadius, xx, yy+Config.crosshairRadius)
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

    val radius = Config.sampleLineDotRadius * 1.2f
    applet.ellipseMode(P5Panel.EllipseMode.Center)

    // Draw a slight border around the whole thing
    val border = 1f
    val borderColor = math.abs(Config.sampleLineColor - 255)
    //applet.strokeWeight(Config.sampleLineWidth + border)

    applet.stroke(borderColor)
    applet.noFill
    applet.line(xx1, yy1, xx2, yy2)

    applet.noStroke
    applet.fill(borderColor)
    applet.ellipse(xx2, yy2, radius + border, radius + border)

    //applet.strokeWeight(Config.sampleLineWidth)

    applet.stroke(Config.sampleLineColor)
    applet.noFill
    applet.line(xx1, yy1, xx2, yy2)

    applet.noStroke
    applet.fill(Config.sampleLineColor)
    applet.ellipse(xx2, yy2, radius, radius)

    applet.popMatrix
  }
  
  def sampleLine(g:Graphics2D, x:Float, y:Float, w:Float, h:Float,
                 xSlice:Float, ySlice:Float, xSample:Float, ySample:Float,
                 xRange:(Float,Float), yRange:(Float,Float)) = {

    // Put everything in screen space coords
    val xx1 = P5Panel.map(xSlice, xRange._1, xRange._2, 0, w) toInt
    val yy1 = P5Panel.map(ySlice, yRange._2, yRange._1, 0, h) toInt
    val xx2 = P5Panel.map(xSample, xRange._1, xRange._2, 0, w) toInt
    val yy2 = P5Panel.map(ySample, yRange._2, yRange._1, 0, h) toInt

    val radius = Config.sampleLineDotRadius * 1.2f toInt

    // Draw a slight border around the whole thing
    val border = 1
    val borderColor = Color(math.abs(Config.sampleLineColor - 255))

    g.translate(x, y)

    // draw the main part
    g.setPaint(Config.sampleLineColor)
    g.drawLine(xx1, yy1, xx2, yy2)
    g.fillOval(xx2-radius, yy2-radius, radius, radius)

    // draw a slight border
    g.setPaint(borderColor)
    g.drawOval(xx2-radius-border, yy2-radius-border, 
               radius + border*2, radius + border*2)

    g.translate(-x, -y)
  }
  
}

