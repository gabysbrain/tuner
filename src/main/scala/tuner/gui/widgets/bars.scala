package tuner.gui.widgets

import tuner.Table
import tuner.gui.P5Panel

class Bars(barStroke:Option[Int], barFill:Option[Int]) {

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           bins:List[Float]) = {

    val maxCount = bins.max

    applet.pushMatrix
    applet.translate(x, y+h)

    val maxVal = bins.max
    val barWidth = w / (bins.length + 1)

    barFill match {
      case Some(c) => applet.fill(c)
      case None    => applet.noFill
    }
    barStroke match {
      case Some(c) => applet.stroke(c)
      case None    => applet.noStroke
    }
    applet.rectMode(P5Panel.RectMode.Corners)
    // draw all the bars
    bins.foldLeft(0f) {case (curX, count) =>
      val hgt = P5Panel.map(count, 0, maxCount, 0, h)
      applet.rect(curX, 0, curX+barWidth, -hgt)
      curX + barWidth
    }

    applet.popMatrix
  }

}

