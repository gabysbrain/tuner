package tuner.util

import tuner.Matrix2D
import tuner.Table

object Density2D {
  
  def density(data:Table, n:Int,
              rowFieldRange:(String,(Float,Float)), 
              colFieldRange:(String,(Float,Float))) : Matrix2D = {

    val rowField = rowFieldRange._1
    val colField = colFieldRange._1

    val mtx = tuner.Sampler.regularSlice(rowFieldRange, colFieldRange, n)
    val rowRadius = math.abs(mtx.rowIds(0) - mtx.rowIds(1)) / 2
    val colRadius = math.abs(mtx.colIds(0) - mtx.colIds(1)) / 2

    for(r <- 0 until data.numRows) {
      val tpl = data.tuple(r)
      val (rowVal, colVal) = (tpl(rowField), tpl(colField))

      // Figure out the top left corner of the cell the point is in
      val minRowPos = mtx.rowIds.lastIndexWhere {_ < rowVal}
      val minColPos = mtx.colIds.lastIndexWhere {_ < colVal}

      // Let's see where this should actually get assigned
      val updateRow = updateIdx(minRowPos, rowVal, rowRadius, mtx.rowIds)
      val updateCol = updateIdx(minColPos, colVal, colRadius, mtx.colIds)
      (updateRow, updateCol) match {
        case (Some(ur), Some(uc)) => mtx.set(ur, uc, mtx.get(ur, uc) + 1)
        case _ => // outside the bounds of the grid
      }
    }
    mtx
  }

  private def updateIdx(minIdx:Int, value:Float, radius:Float, vals:List[Float])
        : Option[Int] = {
    if(minIdx < 0) {
      if(value >= vals.head - radius) {
        Some(0)
      } else {
        None
      }
    } else if (minIdx == vals.length - 1) {
      if(value <= vals.last + radius) {
        Some(vals.length-1)
      } else {
        None
      }
    } else {
      // We're inside the grid!
      if(value <= vals(minIdx) + radius)
        Some(minIdx)
      else
        Some(minIdx + 1)
    }
  }
}

