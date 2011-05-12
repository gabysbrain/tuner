package tuner.gui

import tuner.Config
import tuner.geom.Rectangle

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
           dimInfo:(String,(Float,Float))) = {
    val (field, (minVal, maxVal)) = dimInfo

    // Figure out what size to make the text and axes
    val axisOffset = Config.axisTickSize + Config.axisLabelSpace
    val (textBox, tickBox) = placement match {
      case VerticalLeft =>
        (Rectangle((x,y), (x+w-axisOffset,y+h)), 
         Rectangle((x+w-Config.axisTickSize,y), (x+w, y+h)))
      case VerticalRight =>
        (Rectangle((x+axisOffset,y), (x+w,y+h)), 
         Rectangle((x,y), (x+Config.axisTickSize, y+h)))
      case HorizontalTop =>
        (Rectangle((x,y), (x+w,y+h-axisOffset)), 
         Rectangle((x,y+h-Config.axisTickSize), (x+w, y+h)))
      case HorizontalBottom =>
        (Rectangle((x,y+axisOffset), (x+w,y+h)), 
         Rectangle((x,y), (x+w, y+Config.axisSize)))
    }

    // Figure out which ticks to draw
    //val ticks = Range.Double(minVal, maxVal, (maxVal-minVal)/3) map {_.toFloat}
    val ticks = List(minVal, (maxVal - minVal)/2, maxVal)
    drawTicks(applet, tickBox, ticks)
  }

  def drawTicks(applet:P5Panel, tickBox:Rectangle, ticks:Seq[Float]) = {
    applet.stroke(Config.lineColor)
    placement match {
      case VerticalLeft => drawTicksVert(applet, tickBox, ticks)
      case VerticalRight => drawTicksVert(applet, tickBox, ticks)
      case HorizontalTop => drawTicksHoriz(applet, tickBox, ticks)
      case HorizontalBottom => drawTicksHoriz(applet, tickBox, ticks)
    }
  }

  def drawTicksVert(applet:P5Panel, tickBox:Rectangle, ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val yy = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.maxY, tickBox.minY)
      applet.line(tickBox.minX, yy, tickBox.maxX, yy)
    }
  }

  def drawTicksHoriz(applet:P5Panel, tickBox:Rectangle, ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val xx = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.minX, tickBox.maxX)
      applet.line(xx, tickBox.minY, xx, tickBox.maxY)
    }
  }

}

