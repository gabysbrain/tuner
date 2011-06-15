package tuner.gui.util

import scala.collection.immutable.SortedMap

import tuner.R
import tuner.Table

object Histogram {
  private def rHist(values:Iterable[Float], numBreaks:Int) = {
    val rvect = "c(" + values.map(_.toString).reduceLeft(_ + "," + _) + ")"
    val cmd = "hist(%s, breaks=%d, plot=FALSE)"
    println(cmd)
    R.runCommand(cmd.format(rvect, numBreaks))
  }
  
  def countData(field:String, data:Table, numBreaks:Int) 
        : SortedMap[Float,Int] = {

    val hist = rHist(data.values(field), numBreaks).asList
    val breaks = hist.at("breaks").asDoubles.map(_.toFloat)
    val counts = hist.at("counts").asDoubles.map(_.toInt)
    SortedMap[Float,Int]() ++ breaks.zip(counts)
  }

  def pctData(field:String, data:Table, numBreaks:Int) 
        : SortedMap[Float,Float] = {
    val hist = rHist(data.values(field), numBreaks).asList
    val breaks = hist.at("breaks").asDoubles.map(_.toFloat)
    val pcts = hist.at("density").asDoubles.map(_.toFloat/100f)
    SortedMap[Float,Float]() ++ breaks.zip(pcts)
  }

}

