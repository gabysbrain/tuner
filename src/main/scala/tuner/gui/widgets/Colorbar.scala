package tuner.gui.widgets

import tuner.Config
import tuner.SpecifiedColorMap
import tuner.geom.Rectangle
import tuner.geom.Triangle
import tuner.gui.P5Panel

import processing.core.PGraphicsJava2D

object Colorbar {
  sealed trait Placement
  case object Left extends Placement
  case object Right extends Placement
}

class Colorbar(placement:Colorbar.Placement, editable:Boolean=true) {

  import Colorbar._

  var bounds:Rectangle = Rectangle((0f,0f),(0f,0f))
  var barBounds:Rectangle = Rectangle((0f,0f),(0f,0f))
  //var handleBounds:Triangle = Triangle((0f,0f),(0f,0f),(0f,0f))

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           field:String, colormap:SpecifiedColorMap) = {
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
    val ticks = colormap.ticks
    
    applet.stroke(Config.lineColor)
    applet.fill(Config.lineColor)
    drawLabel(applet, x, y, w, field)
    placement match {
      case Right =>
        drawTicks(applet, x, barStartY, labelWidth, barHeight, ticks)
        drawColorbar(applet, x+labelWidth, barStartY, barWidth, barHeight, 
                     colormap)
        if(editable) {
          drawHandle(applet, x+labelWidth+barWidth, barStartY, 
                             barHeight, colormap)
        }
      case Left =>
        drawTicks(applet, x+Config.colorbarHandleSize._1+barWidth, barStartY, 
                          labelWidth, barHeight, ticks)
        drawColorbar(applet, x+Config.colorbarHandleSize._1, barStartY, 
                             barWidth, barHeight, colormap)
        if(editable) {
          drawHandle(applet, x, barStartY, barHeight, colormap)
        }
    }
  }

  def drawLabel(applet:P5Panel, x:Float, y:Float, w:Float, field:String) = {
    applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Top)
    applet.text(field, x+w/2, y)
  }

  def drawTicks(applet:P5Panel, x:Float, y:Float, 
                labelWidth:Float, h:Float, ticks:Seq[Float]) = {

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
        case Right =>
          applet.text(txt, x, yy)
          val tickStart = x+labelWidth+Config.colorbarLabelSpace._2
          applet.line(tickStart, yy, 
                      tickStart+Config.colorbarTickSize, yy)
        case Left =>
          applet.text(txt, x+textX, yy)
          applet.line(x, yy, x+Config.colorbarTickSize, yy)
      }
    }
  }

  def drawColorbar(applet:P5Panel, x:Float, y:Float, w:Float, h:Float,
                   colormap:SpecifiedColorMap) = {
    barBounds = Rectangle((x,y), (x+w,y+h))
    applet.renderer match {
      case P5Panel.OpenGL => drawColorbarOGL(applet, x, y, w, h, colormap)
      case P5Panel.Java2D => drawColorbarJ2D(applet, x, y, w, h, colormap)
    }
  }

  def drawHandle(applet:P5Panel, x:Float, y:Float, h:Float, 
                 colormap:SpecifiedColorMap) = {

    val yy = P5Panel.map(colormap.filterVal, 
                         colormap.minVal, colormap.maxVal,
                         y+h, y)
    val yOffset = Config.colorbarHandleSize._2 / 2
    applet.fill(Config.lineColor)
    applet.noStroke

    val (p1, p2, p3) = placement match {
      case Right =>
        ((x, yy), 
         (x+Config.colorbarHandleSize._1, yy + yOffset),
         (x+Config.colorbarHandleSize._1, yy - yOffset))
      case Left =>
        ((x+Config.colorbarHandleSize._1, yy), 
         (x, yy + yOffset),
         (x, yy - yOffset))
    }

    //handleBounds = Triangle(p1, p2, p3)
    applet.triangle(p1._1, p1._2, p2._1, p2._2, p3._1, p3._2)
  }

  def isInside(mouseX:Int, mouseY:Int) = bounds.isInside(mouseX, mouseY)

  /**
   * draw the colorbar when using an OpenGL renderer
   */
  private def drawColorbarOGL(applet:P5Panel, x:Float, y:Float, 
                                              w:Float, h:Float,
                                              colormap:SpecifiedColorMap) = {
    applet.noStroke

    applet.beginShape(P5Panel.Shape.QuadStrip)
    // Maybe draw the filterd out colors
    if(colormap.isFiltered) {
      applet.fill(colormap.filterColor)
      val yy1 = P5Panel.map(colormap.filterStart, 
                            colormap.minVal, colormap.maxVal, 
                            y+h, y)
      val yy2 = P5Panel.map(colormap.filterVal, 
                            colormap.minVal, colormap.maxVal, 
                            y+h, y)
      applet.vertex(x, yy1)
      applet.vertex(x+w, yy1)
      applet.vertex(x, yy2)
      applet.vertex(x+w, yy2)
    }
    List(colormap.filterVal, colormap.colorEnd).foreach {break =>
    //colormap.breaks.foreach {break =>
      applet.fill(colormap.color(break))
      val yy = P5Panel.map(break, colormap.minVal, colormap.maxVal, y+h, y)
      applet.vertex(x, yy)
      applet.vertex(x+w, yy)
    }
    applet.endShape
  }

  /**
   * draw the colorbar using the Java2D renderer.  Java2D doesn't support
   * automatic color interpolation like opengl :(
   */
  private def drawColorbarJ2D(applet:P5Panel, x:Float, y:Float, 
                                              w:Float, h:Float,
                                              colormap:SpecifiedColorMap) = {
    // get the java2d graphics context
    val pgl = applet.g.asInstanceOf[PGraphicsJava2D]
    val g2 = pgl.g2

    // Filtered out values get one color
    if(colormap.isFiltered) {
      val fyy1 = P5Panel.map(colormap.filterStart, 
                             colormap.minVal, colormap.maxVal, 
                             y+h, y)
      val fyy2 = P5Panel.map(colormap.filterVal, 
                             colormap.minVal, colormap.maxVal, 
                             y+h, y)
      g2.setPaint(colormap.filterColor.toAwt)
      g2.fill(new java.awt.geom.Rectangle2D.Float(x, fyy2, w, fyy1-fyy2))
    }

    // Remaining color needs a gradient
    val cyy1 = P5Panel.map(colormap.filterVal, 
                           colormap.minVal, colormap.maxVal, 
                           y+h, y)
    val cyy2 = P5Panel.map(colormap.colorEnd, 
                           colormap.minVal, colormap.maxVal, 
                           y+h, y)
    val gradient = new java.awt.GradientPaint(
      0,   cyy1, colormap.color(colormap.filterVal).toAwt,
      100, cyy2, colormap.color(colormap.colorEnd).toAwt)
    g2.setPaint(gradient)
    g2.fill(new java.awt.geom.Rectangle2D.Float(x, cyy2, w, cyy1-cyy2))
  }
}

