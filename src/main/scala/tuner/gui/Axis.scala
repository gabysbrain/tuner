package tuner.gui

import tuner.Config
import tuner.geom.Rectangle

object Axis {
  sealed trait Placement 
  case object VerticalLeft extends Placement
  case object VerticalRight extends Placement
  case object HorizontalTop extends Placement
  case object HorizontalBottom extends Placement
}

class Axis(placement:Axis.Placement) {
  
  import Axis._

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float) = {
    // The size of the axis 
    val axisOffset = Config.axisTickSize + Config.axisTickSpace
    val (textBox, axisBox) = placement match {
      case VerticalLeft =>
        (Rectangle((x,y), (x+w-axisOffet,y+h)), 
         Rectangle((x+w-Config.axisTickSpace,y), (x+w, y+h)))
      case VerticalRight =>
        (Rectangle((x+axisOffset,y), (x+w,y+h)), 
         Rectangle((x,y), (x+Config.axisTickSpace, y+h)))
      case HorizontalTop =>
        (Rectangle((x,y), (x+w,y+h-axisOffset)), 
         Rectangle((x,y+h-Config.axisTickSpace), (x+w, y+h)))
      case HorizontalBottom =>
        (Rectangle((x,y+h-axisOffset), (x+w,y+h)), 
         Rectangle((x,y), (x+w, y+h)))
    }
  }
}

