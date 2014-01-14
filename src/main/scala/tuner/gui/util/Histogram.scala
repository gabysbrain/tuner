package tuner.gui.util

import scala.collection.immutable.SortedMap

import tuner.Table

object Histogram {
  private def countData(values:Iterable[Float], numBreaks:Int) = {
    // Make sure we have enough values for the histogram
    if(values.isEmpty || values.tail.isEmpty) {
      throw new IllegalArgumentException("values must have length > 1")
    }
    // First figure out the breaks
    val breaks = new collection.mutable.MutableList[Float]
    val min = values.min
    val max = values.max
    val step = (max-min) / (numBreaks+1).toFloat
    var cur = min + step
    breaks += min
    while(cur <= max) {
      breaks += cur
      cur += step
    }

    // Now figure out the counts
    val counts = breaks.sliding(2) map { lims =>
      val (mn, mx) = (lims(0), lims(1))
      values.reduceLeft {(sum, x) => sum + (if(x >= mn && x < mx) 1 else 0)}
    }

    (breaks.toList, counts map {_.toInt} toList)
  }
  
  def countData(field:String, data:Table, numBreaks:Int) 
        : SortedMap[Float,Int] = {

    val (breaks, counts) = countData(data.values(field), numBreaks)
    SortedMap[Float,Int]() ++ breaks.zip(counts)
  }

  def pctData(field:String, data:Table, numBreaks:Int) 
        : SortedMap[Float,Float] = {
    val (breaks, counts) = countData(data.values(field), numBreaks)
    val ttlCount = counts.sum.toFloat
    val pcts = counts map {x => x.toFloat / ttlCount}
    SortedMap[Float,Float]() ++ breaks.zip(pcts)
  }

}

