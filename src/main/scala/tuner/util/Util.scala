package tuner.util

object Util {
  def maxIndex(vals:Array[Double]) = {
    var mxVal = vals(0)
    var mxRow = 0
    for(i <- 1 until vals.length) {
      if(vals(i) > mxVal) {
        mxVal = vals(i)
        mxRow = i
      }
    }
    mxRow
  }
}

