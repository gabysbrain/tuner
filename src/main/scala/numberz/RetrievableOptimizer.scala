package numberz

trait RetrievableOptimizer {

  var lastMinimum:Array[Double] = null

  def computeObjectiveValue(point:Array[Double], 
                            f:Array[Double]=>Double) : Double = {
    val res = f(point)
    lastMinimum = point
    res
  }
}

