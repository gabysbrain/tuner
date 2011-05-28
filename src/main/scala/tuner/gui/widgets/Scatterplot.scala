package tuner.gui.widgets

import tuner.Config
import tuner.Table
import tuner.gui.P5Panel
import tuner.util.ColorLib

class Scatterplot(dotColor:Int) {

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           data:Table, xFld:String, yFld:String) = {

    val (minX, maxX) = (data.min(xFld), data.max(xFld))
    val (minY, maxY) = (data.min(yFld), data.max(yFld))

    // Draw a white background
    // TODO: make this work for any color
    applet.fill(255)
    applet.rect(x, y, w, h)

    applet.ellipseMode(P5Panel.EllipseMode.Center)
    applet.fill(dotColor)
    applet.stroke(ColorLib.darker(dotColor))

    for(row <- 0 until data.numRows) {
      val tpl = data.tuple(row)
      val xVal = tpl(xFld)
      val yVal = tpl(yFld)
      val xx = P5Panel.map(xVal, minX, maxX, x, x+w)
      val yy = P5Panel.map(yVal, maxY, minY, y, y+h)
      applet.ellipse(xx, 
                     yy, 
                     Config.scatterplotDotSize, 
                     Config.scatterplotDotSize)
    }
  }

}

