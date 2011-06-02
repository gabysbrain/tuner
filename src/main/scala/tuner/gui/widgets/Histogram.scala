package tuner.gui.widgets

import tuner.Table
import tuner.gui.P5Panel

object Histogram {

  def computeBreaks(minVal:Float, maxVal:Float, numBreaks:Int) : List[Float] = {
    if(numBreaks <= 1) {
      Nil
    } else {
      val spacing = (maxVal - minVal) / (numBreaks - 1)
      val last = maxVal - (spacing/2)
      def bl(lst:List[Float],v:Float) : List[Float] = {
        if(v <= minVal) lst
        else            bl(v::lst, v-spacing)
      }
      bl(Nil, last)
    }
  }

}

class Histogram(barStroke:Option[Int], barFill:Option[Int], numBreaks:Int) {

  var counts:Map[Float,Int] = Map()

  def draw(applet:P5Panel, x:Float, y:Float, w:Float, h:Float, 
           field:String, data:Table) = {

    applet.pushMatrix
    applet.translate(x, y+h)

    val (minVal, maxVal) = (data.min(field), data.max(field))
    val breaks = Histogram.computeBreaks(minVal, maxVal, numBreaks)

    counts = countData(field, data, breaks)

    val maxCount = counts.values.max
    val barWidth = w / (breaks.length + 1)

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
    counts.keys.foldLeft(0f) {case (curX, count) =>
      val hgt = P5Panel.map(counts(count), 0, maxCount, 0, h)
      applet.rect(curX, 0, curX+barWidth, -hgt)
      curX + barWidth
    }

    applet.popMatrix
  }

  def countData(field:String, data:Table, breaks:List[Float]) = {
    var counts = (breaks ++ List(Float.MaxValue)).map((_, 0)).toMap
    for(r <- 0 until data.numRows) {
      val tpl = data.tuple(r)
      counts.keys.foreach {k =>
        if(k > tpl(field)) 
          counts += (k -> (counts(k) + 1))
      }
    }
    counts
  }
}

