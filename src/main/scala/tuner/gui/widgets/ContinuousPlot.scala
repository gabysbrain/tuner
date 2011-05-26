package tuner.gui.widgets

import tuner.Config
import tuner.Matrix2D
import tuner.SpecifiedColorMap
import tuner.geom.Rectangle
import tuner.gui.P5Panel

class ContinuousPlot {

  //var colorMap = cm

  var bounds:Rectangle = Rectangle((0f,0f),(0f,0f))

  def mapx(width:Float, range:(Float,Float), v:Float) : Float = 
    P5Panel.map(v, range._1, range._2, 0, width)

  def mapy(height:Float, range:(Float,Float), v:Float) : Float = 
    P5Panel.map(v, range._2, range._1, 0, height)

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           data:Matrix2D, xSlice:Float, ySlice:Float, 
           xRange:(Float,Float), yRange:(Float,Float),
           colormap:SpecifiedColorMap) = {
    bounds = Rectangle((x,y), (x+w,y+h))

    //val startTime = System.currentTimeMillis
    applet.pushMatrix
    applet.translate(x, y)

    // Draw the heatmap
    applet.noStroke
    applet.beginShape(P5Panel.Shape.TriangleStrip)
    // The first row is special
    if(data.rows >= 2) {
      for(c <- 0 until data.columns) {
        drawPoint(applet, 0, c, w, h, xRange, yRange, data, colormap)
        drawPoint(applet, 1, c, w, h, xRange, yRange, data, colormap)
      }
    }
    for(r <- 2 until data.rows) {
      // Even rows go backwards
      if(r % 2 == 0) {
        drawPoint(applet, r, data.columns-1, w, h, 
                  xRange, yRange, data, colormap)
        for(c <- data.columns-2 until -1 by -1) {
          drawPoint(applet, r-1, c, w, h, xRange, yRange, data, colormap)
          drawPoint(applet, r, c, w, h, xRange, yRange, data, colormap)
        }
      } else {
        drawPoint(applet, r, 0, w, h, xRange, yRange, data, colormap)
        for(c <- 1 until data.columns) {
          drawPoint(applet, r-1, c, w, h, xRange, yRange, data, colormap)
          drawPoint(applet, r, c, w, h, xRange, yRange, data, colormap)
        }
      }
    }
    applet.endShape

    drawCrosshair(applet, w, h, xSlice, ySlice, xRange, yRange)

    applet.popMatrix
  }

  def drawPoint(applet:P5Panel, r:Int, c:Int, w:Float, h:Float, 
                xRange:(Float,Float), yRange:(Float,Float), 
                data:Matrix2D, colormap:SpecifiedColorMap) = {
    val x = data.rowVal(r)
    val y = data.colVal(c)
    val cc = data.get(r, c)

    // set the fill on each vertex separately
    applet.fill(colormap.color(cc))
    applet.vertex(mapx(w, xRange, x), mapy(h, yRange, y))
  }

  def drawCrosshair(applet:P5Panel, w:Float, h:Float, 
                                    xSlice:Float, ySlice:Float,
                                    xRange:(Float,Float), 
                                    yRange:(Float,Float)) = {
    val xx = mapx(w, xRange, xSlice)
    val yy = mapy(h, yRange, ySlice)

    applet.stroke(Config.crosshairColor)
    applet.line(xx-Config.crosshairRadius, yy, xx+Config.crosshairRadius, yy)
    applet.line(xx, yy-Config.crosshairRadius, xx, yy+Config.crosshairRadius)
  }

  def isInside(mouseX:Int, mouseY:Int) = bounds.isInside(mouseX, mouseY)
}

