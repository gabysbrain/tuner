package tuner.gui.widgets

import tuner.Config
import tuner.SpecifiedColorMap
import tuner.geom.Rectangle
import tuner.geom.Triangle
import tuner.gui.P5Panel

object Colorbar {
  sealed trait Placement
  case object Left extends Placement
  case object Right extends Placement
}

class Colorbar(val colormap:SpecifiedColorMap, 
               field:String, placement:Colorbar.Placement) {

  import Colorbar._

  var bounds:Rectangle = Rectangle((0f,0f),(0f,0f))
  var barBounds:Rectangle = Rectangle((0f,0f),(0f,0f))
  var handleBounds:Triangle = Triangle((0f,0f),(0f,0f),(0f,0f))

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float) = {
    bounds = Rectangle((x,y), (x+w,y+h))

    applet.textFont(Config.fontPath, Config.smallFontSize)
    val labelWidth = 
      applet.textWidth("-" + "M" * Config.colorbarTickDigits._1 +
                       "." + "M" * Config.colorbarTickDigits._2)
    // Figure out how much space we can give to the colorbar
    val barWidth = w - labelWidth - 
                       Config.colorbarLabelSpace._2 - 
                       Config.colorbarTickSize -
                       Config.colorbarHandleSize._1
    
    val barHeight = h - Config.colorbarLabelSpace._1 - 
                    Config.smallFontSize - 
                    (Config.colorbarHandleSize._2 / 2)
    val barStartY = y + Config.colorbarLabelSpace._1 + Config.smallFontSize
    val ticks = List(colormap.minVal, 
                     (colormap.minVal + colormap.maxVal) / 2,
                     colormap.maxVal)
    
    applet.stroke(Config.lineColor)
    applet.fill(Config.lineColor)
    drawLabel(applet, x, y, w)
    placement match {
      case Left =>
        drawTicks(applet, x, barStartY, barHeight, ticks)
        drawColorbar(applet, x+labelWidth, barStartY, barWidth, barHeight)
        drawHandle(applet, x+labelWidth+barWidth, barStartY, barHeight)
      case Right =>
        drawTicks(applet, x+Config.colorbarHandleSize._1+barWidth, barStartY, 
                          barHeight, ticks)
        drawColorbar(applet, x+Config.colorbarHandleSize._1, barStartY, 
                             barWidth, barHeight)
        drawHandle(applet, x, barStartY, barHeight)
    }
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
      val txt = P5Panel.nfs(tick, 
                            Config.colorbarTickDigits._1, 
                            Config.colorbarTickDigits._2) 
      placement match {
        case Left =>
          applet.text(txt, x, yy)
          applet.line(x+textX, yy, x+textX+Config.colorbarTickSize, yy)
        case Right =>
          applet.text(txt, x+textX, yy)
          applet.line(x, yy, x+Config.colorbarTickSize, yy)
      }
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

  def drawHandle(applet:P5Panel, x:Float, y:Float, h:Float) = {

    val yy = P5Panel.map(colormap.filterVal, 
                         colormap.minVal, colormap.maxVal,
                         y+h, y)
    val yOffset = Config.colorbarHandleSize._2 / 2
    applet.fill(Config.lineColor)
    applet.noStroke

    val (p1, p2, p3) = placement match {
      case Left =>
        ((x, yy), 
         (x+Config.colorbarHandleSize._1, yy + yOffset),
         (x+Config.colorbarHandleSize._1, yy - yOffset))
      case Right =>
        ((x+Config.colorbarHandleSize._1, yy), 
         (x, yy + yOffset),
         (x, yy - yOffset))
    }

    handleBounds = Triangle(p1, p2, p3)
    applet.triangle(p1._1, p1._2, p2._1, p2._2, p3._1, p3._2)
  }

  def isInside(mouseX:Int, mouseY:Int) = bounds.isInside(mouseX, mouseY)
}

