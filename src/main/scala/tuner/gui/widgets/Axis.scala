package tuner.gui.widgets

import tuner.Config
import tuner.gui.P5Panel
import tuner.geom.Rectangle
import tuner.gui.util.AxisTicks

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

  private def drawLabelHoriz(applet:P5Panel, labelBox:Rectangle, 
                             label:String) = {

    //applet.textMode(P5Panel.TextMode.Model)
    applet.textFont(Config.fontPath, Config.smallFontSize)
    applet.textAlign(P5Panel.TextHAlign.Center, P5Panel.TextVAlign.Center)
    val centerPt = labelBox.center
    applet.text(label, centerPt._1, centerPt._2)
  }

}

