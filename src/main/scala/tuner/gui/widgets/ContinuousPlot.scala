package tuner.gui.widgets

import tuner.Matrix2D
import tuner.SpecifiedColorMap
import tuner.gui.P5Panel

class ContinuousPlot(var minX:Float, var maxX:Float, 
                     var minY:Float, var maxY:Float, 
                     cm:SpecifiedColorMap) {

  var colorMap = cm

  var hide:Boolean = false

  def mapx(width:Float, v:Float) : Float = 
    P5Panel.map(v, minX, maxX, 0, width)

  def mapy(height:Float, v:Float) : Float = 
    P5Panel.map(v, maxY, minY, 0, height)

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, data:Matrix2D) = {
    if(!hide) {
      //val startTime = System.currentTimeMillis
      applet.pushMatrix
      applet.translate(x, y)
  
      // Draw the heatmap
      applet.noStroke
      applet.beginShape(P5Panel.Shape.TriangleStrip)
      // The first row is special
      if(data.rows >= 2) {
        for(c <- 0 until data.columns) {
          drawPoint(applet, 0, c, w, h, data)
          drawPoint(applet, 1, c, w, h, data)
        }
      }
      for(r <- 2 until data.rows) {
        // Even rows go backwards
        if(r % 2 == 0) {
          drawPoint(applet, r, data.columns-1, w, h, data)
          for(c <- data.columns-2 until -1 by -1) {
            drawPoint(applet, r-1, c, w, h, data)
            drawPoint(applet, r, c, w, h, data)
          }
        } else {
          drawPoint(applet, r, 0, w, h, data)
          for(c <- 1 until data.columns) {
            drawPoint(applet, r-1, c, w, h, data)
            drawPoint(applet, r, c, w, h, data)
          }
        }
      }
      applet.endShape
  
      applet.popMatrix
    }
  }

  def colorMap(value:Float) : Int = colorMap.color(value)

  def drawPoint(applet:P5Panel, r:Int, c:Int, w:Float, h:Float, data:Matrix2D) = {
    val x = data.rowVal(r)
    val y = data.colVal(c)
    val cc = data.get(r, c)

    // set the fill on each vertex separately
    applet.fill(colorMap(cc))
    applet.vertex(mapx(w, x), mapy(h, y))
  }

  // from Layer trait
  /*
  def setEstimates(estimates:Matrix2D, cm:SpecifiedColorMap) = {
    //println("new estimates!!!")
    data = estimates
    colorMap = cm
    
    // recache everything
    minRow = data.rowIds.min
    maxRow = data.rowIds.max
    minCol = data.colIds.min
    maxCol = data.colIds.max
    //println("done with new estimates")
  }
  */

}

