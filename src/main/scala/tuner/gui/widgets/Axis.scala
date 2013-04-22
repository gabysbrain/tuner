package tuner.gui.widgets

import tuner.Config
import tuner.geom.Rectangle
import tuner.gui.P5Panel
import tuner.gui.util.FontLib
import tuner.gui.util.TextAlign
import tuner.util.AxisTicks

import java.awt.Graphics2D

object Axis {
  sealed trait Placement 
  // Vertical, text on left side
  case object VerticalLeft extends Placement
  // Vertical, text on right side
  case object VerticalRight extends Placement
  // Horizontal, text on top
  case object HorizontalTop extends Placement
  // Horizontal, text on bottom
  case object HorizontalBottom extends Placement
}

class Axis(placement:Axis.Placement) {
  
  import Axis._

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           field:String, ticks:List[Float]) : Unit = {
    // set up all the colors and such
    applet.fill(Config.lineColor)
    applet.stroke(Config.lineColor)
    applet.strokeWeight(1)

    // Figure out what size to make the text and axes
    val axisOffset = Config.axisTickSize + Config.axisLabelSpace
    val labelSize = Config.smallFontSize
    val labelOffset = labelSize + Config.axisLabelSpace
    placement match {
      case VerticalLeft =>
        val tickBox = Rectangle((x+w-Config.axisTickSize,y), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+labelSize,y+h))
        val textBox = Rectangle((x+labelOffset,y), (x+w-axisOffset,y+h)) 
        drawTicksVert(applet, tickBox, textBox, ticks)
        drawLabelVert(applet, labelBox, field)
      case VerticalRight =>
        val tickBox = Rectangle((x,y), (x+Config.axisTickSize, y+h))
        val labelBox = Rectangle((x+w-labelSize,y), (x+w,y+h))
        val textBox = Rectangle((x+axisOffset,y), (x+w-labelOffset,y+h))
        drawTicksVert(applet, tickBox, textBox, ticks)
        drawLabelVert(applet, labelBox, field)
      case HorizontalTop =>
        val tickBox = Rectangle((x,y+h-Config.axisTickSize), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+w,y+labelSize))
        val textBox = Rectangle((x,y+labelOffset), (x+w,y+h-axisOffset))
        drawTicksHoriz(applet, tickBox, textBox, ticks)
        drawLabelHoriz(applet, labelBox, field)
      case HorizontalBottom =>
        val tickBox = Rectangle((x,y), (x+w, y+Config.axisTickSize))
        val labelBox = Rectangle((x,y+h-labelSize), (x+w,y+h))
        val textBox = Rectangle((x,y+axisOffset), (x+w,y+h-labelOffset))
        drawTicksHoriz(applet, tickBox, textBox, ticks)
        drawLabelHoriz(applet, labelBox, field)
    }

  }

  def draw(g:Graphics2D, x:Float, y:Float, w:Float, h:Float, 
           field:String, ticks:List[Float]) : Unit = {
    // set up all the colors and such
    g.setPaint(Config.lineColor)

    // Figure out what size to make the text and axes
    val axisOffset = Config.axisTickSize + Config.axisLabelSpace
    val labelSize = Config.smallFontSize
    val labelOffset = labelSize + Config.axisLabelSpace
    placement match {
      case VerticalLeft =>
        val tickBox = Rectangle((x+w-Config.axisTickSize,y), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+labelSize,y+h))
        val textBox = Rectangle((x+labelOffset,y), (x+w-axisOffset,y+h)) 
        drawTicksVert(g, tickBox, textBox, ticks)
        drawLabelVert(g, labelBox, field)
      case VerticalRight =>
        val tickBox = Rectangle((x,y), (x+Config.axisTickSize, y+h))
        val labelBox = Rectangle((x+w-labelSize,y), (x+w,y+h))
        val textBox = Rectangle((x+axisOffset,y), (x+w-labelOffset,y+h))
        drawTicksVert(g, tickBox, textBox, ticks)
        drawLabelVert(g, labelBox, field)
      case HorizontalTop =>
        val tickBox = Rectangle((x,y+h-Config.axisTickSize), (x+w, y+h))
        val labelBox = Rectangle((x,y), (x+w,y+labelSize))
        val textBox = Rectangle((x,y+labelOffset), (x+w,y+h-axisOffset))
        drawTicksHoriz(g, tickBox, textBox, ticks)
        drawLabelHoriz(g, labelBox, field)
      case HorizontalBottom =>
        val tickBox = Rectangle((x,y), (x+w, y+Config.axisTickSize))
        val labelBox = Rectangle((x,y+h-labelSize), (x+w,y+h))
        val textBox = Rectangle((x,y+axisOffset), (x+w,y+h-labelOffset))
        drawTicksHoriz(g, tickBox, textBox, ticks)
        drawLabelHoriz(g, labelBox, field)
    }

  }

  private def drawTicksVert(applet:P5Panel, tickBox:Rectangle, 
                            textBox:Rectangle, ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val yy = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.maxY, tickBox.minY)
      applet.line(tickBox.minX, yy, tickBox.maxX, yy)

      // Also draw the label
      if(tick == ticks.head) {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Bottom)
      } else if(tick == ticks.last) {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Top)
      } else {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
      }

      applet.text(P5Panel.nfs(tick, Config.axisTickDigits._1, 
                                    Config.axisTickDigits._2), 
                  textBox.center._1, yy)
    }
  }

  private def drawTicksVert(g:Graphics2D, tickBox:Rectangle, 
                            textBox:Rectangle, ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val yy = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.maxY, tickBox.minY).toInt
      g.drawLine(tickBox.minX.toInt, yy, tickBox.maxX.toInt, yy)

      // Also draw the label
      val (h, v) = if(tick == ticks.head) {
        (TextAlign.Right, TextAlign.Bottom)
      } else if(tick == ticks.last) {
        (TextAlign.Right, TextAlign.Top)
      } else {
        (TextAlign.Right, TextAlign.Middle)
      }

      val txt = P5Panel.nfs(tick, Config.axisTickDigits._1,
                                  Config.axisTickDigits._2)
      FontLib.drawString(g, txt, textBox.maxX.toInt, yy, h, v)
    }
  }

  private def drawTicksHoriz(applet:P5Panel, tickBox:Rectangle, 
                             textBox:Rectangle, ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val xx = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.minX, tickBox.maxX)
      applet.line(xx, tickBox.minY, xx, tickBox.maxY)

      // Also draw the label rotated vertically
      applet.pushMatrix
      applet.translate(xx, textBox.center._2)
      applet.rotate(-P5Panel.HalfPi)

      if(tick == ticks.head) {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Top)
      } else if(tick == ticks.last) {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Bottom)
      } else {
        applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
      }

      applet.text(P5Panel.nfs(tick, Config.axisTickDigits._1, 
                                    Config.axisTickDigits._2), 
                  0, 0)
      
      applet.popMatrix
    }
  }

  private def drawTicksHoriz(g:Graphics2D, tickBox:Rectangle, 
                             textBox:Rectangle, ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val xx = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.minX, tickBox.maxX).toInt
      g.drawLine(xx, tickBox.minY.toInt, xx, tickBox.maxY.toInt)

      // Also draw the label rotated vertically
      //g.rotate(-P5Panel.HalfPi, xx, textBox.center._2.toInt)

      val (h, v) = if(tick == ticks.head) {
        (TextAlign.Left, TextAlign.Bottom)
      } else if(tick == ticks.last) {
        (TextAlign.Right, TextAlign.Bottom)
      } else {
        (TextAlign.Center, TextAlign.Bottom)
      }

      val txt = P5Panel.nfs(tick, Config.axisTickDigits._1, 
                                  Config.axisTickDigits._2)
      FontLib.drawVString(g, txt, xx, textBox.minY.toInt, h, v)
    }
  }

  private def drawLabelVert(applet:P5Panel, labelBox:Rectangle,
                            label:String) = {
    
    val centerPt = labelBox.center

    applet.pushMatrix
    applet.translate(centerPt._1, centerPt._2)
    applet.rotate(-P5Panel.HalfPi)

    //applet.textMode(P5Panel.TextMode.Model)
    applet.textFont(Config.fontPath, Config.smallFontSize)
    applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
    applet.text(label, 0, 0)

    applet.popMatrix
  }

  private def drawLabelVert(g:Graphics2D, labelBox:Rectangle,
                            label:String) = {
    
    //applet.textFont(Config.fontPath, Config.smallFontSize)
    val oldFont = g.getFont
    g.setFont(oldFont.deriveFont(Config.smallFontSize))

    val centerPt = labelBox.center
    FontLib.drawVString(g, label, centerPt._1.toInt, centerPt._2.toInt, 
                                  TextAlign.Center, TextAlign.Middle)

    g.setFont(oldFont)
  }

  private def drawLabelHoriz(applet:P5Panel, labelBox:Rectangle, 
                             label:String) = {

    //applet.textMode(P5Panel.TextMode.Model)
    applet.textFont(Config.fontPath, Config.smallFontSize)
    applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
    val centerPt = labelBox.center
    applet.text(label, centerPt._1, centerPt._2)
  }

  private def drawLabelHoriz(g:Graphics2D, labelBox:Rectangle, 
                             label:String) = {

    val oldFont = g.getFont
    g.setFont(oldFont.deriveFont(Config.smallFontSize))


    val centerPt = labelBox.center
    FontLib.drawString(g, label, centerPt._1.toInt, centerPt._2.toInt, 
                                 TextAlign.Center, TextAlign.Middle)

    g.setFont(oldFont)
  }

}

