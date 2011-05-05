package tuner

import scala.io.Source

object DimRanges {
  type Range = (Float, Float)

  // Unkown dims get a 0 to 1 range
  val defaultRange = (0f, 1f)
  
  def fromCsv(filename:String) : DimRanges = {
    print("reading " + filename + "...")
    val file = Source.fromFile(filename).getLines

    // dim range lines are dimname,low,high
    val dr = new DimRanges(file.map({line =>
      val tmp = line.split(",")
      (tmp(0), (tmp(1).toFloat, tmp(2).toFloat))
    }).toMap)
  
    println("done")
    dr
  }

  def from2dSlice(d1:(String,(Float,Float)), 
                  d2:(String,(Float,Float)),
                  slice:List[(String,Float)]) : DimRanges = {
    new DimRanges((d1 :: d2 :: slice.map {s => (s._1, (s._2, s._2))}).toMap)
  }
}

class DimRanges(r:Map[String,DimRanges.Range]) {

  val ranges = r

  def range(dim:String) : (Float,Float) = {
    ranges(dim)
  }

  def min(dim:String) : Float = {
    ranges(dim)._1
  }

  def max(dim:String) : Float = {
    // Unkown dims get a 0 to 1 range
    ranges(dim)._2
  }

  def merge(dr:DimRanges) : DimRanges = {
    val newRange = dimNames.map({dn =>
      (dn, (math.min(min(dn), dr.min(dn)), math.max(max(dn), dr.max(dn))))
    }).toMap

    new DimRanges(newRange)
  }

  def dimNames : List[String] = ranges.keys.toList

  def numDim : Int = ranges.size
}

