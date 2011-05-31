package tuner

import scala.collection.immutable.NumericRange
import scala.util.Random

object Sampler {
  val rng = new Random

  type Method = (DimRanges, Int, (List[(String,Float)] => Unit)) => Unit

  def regularSlice(rowDim:(String,(Float,Float)), 
                   colDim:(String,(Float,Float)),
                   n:Int) : Matrix2D = {

    def calcRng(minv:Float, maxv:Float) : List[Float] = {
      val mn = math.min(minv, maxv)
      val mx = math.max(minv, maxv)
      val step = (mx - mn) / (n-1)
      genLinSeq(mn, mx, step)
    }
    new Matrix2D(calcRng(rowDim._2._1, rowDim._2._2), 
                 calcRng(colDim._2._1, colDim._2._2))
  }

  /**
   * Samples using a regular grid.
   * n is the number of samples in each dimension
   */
  def regularGrid(dims:DimRanges, n:Int, 
                  f:List[(String,Float)] => Unit) = {
    val seqs = dims.ranges map { d =>
      val (dimname, (minval, maxval)) = d
      val mn = math.min(minval, maxval)
      val mx = math.max(minval, maxval)
      if(mn == mx) {
        (dimname, List(mn))
      } else {
        val step = if(n == 1)
          (mx - mn)
        else 
          (mx - mn) / (n-1)
        (dimname, genLinSeq(mn, mx, step))
      }
    }

    generateCombinations(f, Nil, seqs.toList)
  }

  def lhc(dims:DimRanges, n:Int, f:List[(String,Float)] => Unit) = {
    // Split the ranges into slices and sample dims
    val (sampleDims, sliceDims) = dims.ranges.span {tmp =>
      val (dimname, rng) = tmp
      rng._1 != rng._2
    }
    val slices = sliceDims.map {tmp => (tmp._1, tmp._2._1)}

    // Use R to generate the maximim lhc
    R.runCommand("library(lhs)")
    val lhcCmd = "maximinLHS(%d, %d)".format(n, sampleDims.size)
    println("R: " + lhcCmd)
    val res = R.runCommand(lhcCmd)
    val x:Array[Array[Double]] = res.asDoubleMatrix
    
    x.foreach({row => 
      val vals = sampleDims.zip(row.map(_.toFloat)).map {vs =>
        val ((dimname, (dimMin, dimMax)), value) = vs
        // Conveniently all the values from the lhs are on a 0->1 scale
        (dimname, dimMin + value * (dimMax - dimMin))
      }
      f(vals.toList ++ slices)
    })
  }

  def lhc(dims:DimRanges, n:Int) : Table = {
    val tbl = new Table
    if(n > 0)
      lhc(dims, n, {r => tbl.addRow(r)})
    tbl
  }

  // There's some weird issues with scala's sequence generator
  // TODO: figure out what it is.  You may be pleasantly surprised!
  def genLinSeq(min:Float, max:Float, step:Float) : List[Float] = {
    if(min > max || step <= 0) {
      Nil
    } else if(min == 0 && max == 0) {
      Nil
    } else {
      min :: genLinSeq(min+step, max, step)
    }
  }

  def generateCombinations(f:List[(String,Float)] => Unit,
                           rowData:List[(String,Float)], 
                           dims:List[(String, List[Float])]) : Unit = {
    if(dims == Nil) {
      f(rowData)
    } else {
      val (dimname, vals) :: rest = dims
      vals.foreach { v => 
        generateCombinations(f, (dimname, v.toFloat)::rowData, rest)
      }
    }
  }

}

