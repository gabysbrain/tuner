package tuner.gui.widgets

import tuner.Config
import tuner.Table
import tuner.gui.P5Panel
import tuner.util.ColorLib

class Scatterplot(dotColor:Int) {

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           data:Table, 
           xRange:(String,(Float,Float)), 
           yRange:(String,(Float,Float))) = {

    val (xFld, (minX, maxX)) = xRange
    val (yFld, (minY, maxY)) = yRange

    // Draw a white background
    // TODO: make this work for any color
    //applet.fill(255)
    //applet.rect(x, y, w, h)

    applet.ellipseMode(P5Panel.EllipseMode.Center)
    applet.fill(dotColor)
    applet.stroke(ColorLib.darker(dotColor))

    for(row <- 0 until data.numRows) {
      val tpl = data.tuple(row)
      val xVal = tpl(xFld)
      val yVal = tpl(yFld)
      val xx = P5Panel.map(xVal, minX, maxX, x, x+w)
      val yy = P5Panel.map(yVal, maxY, minY, y, y+h)
      if(xx >= x && xx <= x+w && yy >= y && yy <= y+h) {
        val dotSize = {
          val (mousex, mousey) = applet.mousePos
          if(math.abs(xx-mousex) < Config.scatterplotDotSize &&
             math.abs(yy-mousey) < Config.scatterplotDotSize) {
            Config.scatterplotDotSize * 1.5
          } else {
            Config.scatterplotDotSize
          }
        }
        applet.ellipse(xx, yy, dotSize.toFloat, dotSize.toFloat)
      }
    }
  }

}

