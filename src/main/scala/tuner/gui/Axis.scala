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

    // Figure out which ticks to draw
    //val ticks = Range.Double(minVal, maxVal, (maxVal-minVal)/3) map {_.toFloat}
    val ticks = List(minVal, (maxVal - minVal)/2, maxVal)

    // set up all the colors and such
    applet.fill(Config.lineColor)
    applet.stroke(Config.lineColor)

    // Figure out what size to make the text and axes
    val axisOffset = Config.axisTickSize + Config.axisLabelSpace
    placement match {
      case VerticalLeft =>
        val textBox = Rectangle((x,y), (x+w-axisOffset,y+h)) 
        val tickBox = Rectangle((x+w-Config.axisTickSize,y), (x+w, y+h))
        drawTicksVert(applet, tickBox, ticks)
      case VerticalRight =>
        val textBox = Rectangle((x+axisOffset,y), (x+w,y+h))
        val tickBox = Rectangle((x,y), (x+Config.axisTickSize, y+h))
        drawTicksVert(applet, tickBox, ticks)
      case HorizontalTop =>
        val textBox = Rectangle((x,y), (x+w,y+h-axisOffset))
        val tickBox = Rectangle((x,y+h-Config.axisTickSize), (x+w, y+h))
        drawTicksHoriz(applet, tickBox, ticks)
      case HorizontalBottom =>
        val textBox = Rectangle((x,y+axisOffset), (x+w,y+h))
        val tickBox = Rectangle((x,y), (x+w, y+Config.axisTickSize))
        drawTicksHoriz(applet, tickBox, ticks)
    }

  }

  private def drawTicksVert(applet:P5Panel, tickBox:Rectangle, 
                            ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val yy = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.maxY, tickBox.minY)
      applet.line(tickBox.minX, yy, tickBox.maxX, yy)
    }
  }

  private def drawTicksHoriz(applet:P5Panel, tickBox:Rectangle, 
                             ticks:Seq[Float]) = {
    ticks.foreach {tick =>
      val xx = P5Panel.map(tick, ticks.head, ticks.last, 
                                 tickBox.minX, tickBox.maxX)
      applet.line(xx, tickBox.minY, xx, tickBox.maxY)
    }
  }

}

