package tuner.gui.widgets

import tuner.Config
import tuner.SpecifiedColorMap
import tuner.geom.Rectangle
import tuner.gui.P5Panel

class Colorbar(val colormap:SpecifiedColorMap, field:String) {

  var bounds:Rectangle = Rectangle((0f,0f),(0f,0f))
  var barBounds:Rectangle = Rectangle((0f,0f),(0f,0f))

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float) = {
    bounds = Rectangle((x,y), (x+w,y+h))

    applet.textFont(Config.fontPath, Config.smallFontSize)
    val labelWidth = 
      applet.textWidth("-" + "M" * Config.colorbarTickDigits._1 +
                       "." + "M" * Config.colorbarTickDigits._2)
    // Figure out how much space we can give to the colorbar
    val barWidth = w - labelWidth - 
                       Config.colorbarLabelSpace._2 - 
                       Config.colorbarTickSize
    
    val barHeight = h - Config.colorbarLabelSpace._1 - Config.smallFontSize
    val barStartY = y + Config.colorbarLabelSpace._1 + Config.smallFontSize
    val ticks = List(colormap.minVal, 
                     (colormap.minVal + colormap.maxVal) / 2,
                     colormap.maxVal)
    
    applet.stroke(Config.lineColor)
    applet.fill(Config.lineColor)
    drawLabel(applet, x, y, w)
    drawTicks(applet, x+barWidth, barStartY, barHeight, ticks)
    drawColorbar(applet, x, barStartY, barWidth, barHeight)
  }

  def drawLabel(applet:P5Panel, x:Float, y:Float, w:Float) = {
    applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Top)
    applet.text(field, (x+w)/2, y)
  }

  def drawTicks(applet:P5Panel, x:Float, y:Float, 
                h:Float, ticks:Seq[Float]) = {

    ticks.foreach {tick =>
      if(tick == ticks.head) {
        applet.textAlign(P5Panel.TextHAlign.Left, P5Panel.TextVAlign.Bottom)
      } else if(tick == ticks.last) {
        applet.textAlign(P5Panel.TextHAlign.Left, P5Panel.TextVAlign.Top)
      } else {
        applet.textAlign(P5Panel.TextHAlign.Left, P5Panel.TextVAlign.Center)
      }

      val yy = P5Panel.map(tick, ticks.head, ticks.last, y+h, y)
      val textX = Config.colorbarTickSize + Config.colorbarLabelSpace._2
      applet.line(x, yy, x+Config.colorbarTickSize, yy)
      applet.text(P5Panel.nfs(tick, 
                              Config.colorbarTickDigits._1, 
                              Config.colorbarTickDigits._2), 
                  x+textX, yy)
    }
  }

  def drawColorbar(applet:P5Panel, x:Float, y:Float, w:Float, h:Float) = {
    barBounds = Rectangle((x,y), (x+w,y+h))
    applet.noStroke

    applet.beginShape(P5Panel.Shape.QuadStrip)
    colormap.breaks.foreach {break =>
      applet.fill(colormap.color(break))
      val yy = P5Panel.map(break, colormap.minVal, colormap.maxVal, y+h, y)
      applet.vertex(x, yy)
      applet.vertex(x+w, yy)
    }
    applet.endShape
  }

  def isInside(mouseX:Int, mouseY:Int) = bounds.isInside(mouseX, mouseY)
}

