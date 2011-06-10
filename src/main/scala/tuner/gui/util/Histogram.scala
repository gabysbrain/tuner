package tuner.gui.util

import tuner.Table

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
  
  def countData(field:String, data:Table, numBreaks:Int) : Map[Float,Int] = {
    val breaks = computeBreaks(data.min(field), data.max(field), numBreaks)
    countData(field, data, breaks)
  }

  def countData(field:String, data:Table, breaks:List[Float]) 
        : Map[Float,Int] = {
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

  def pctData(field:String, data:Table, numBreaks:Int) : Map[Float,Float] = {
    val breaks = computeBreaks(data.min(field), data.max(field), numBreaks)
    pctData(field, data, breaks)
  }

  def pctData(field:String, data:Table, breaks:List[Float]) 
        : Map[Float,Float] = {
    val counts = countData(field, data, breaks)
    val maxCount = counts.values.max.toFloat
    counts.map {case (break,count) => (break -> 100f * count / maxCount)}
  }

}

