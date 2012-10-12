package numberz

import scala.collection.IndexedSeq
import scala.collection.IndexedSeqOptimized

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.CholeskyDecomposition
import org.apache.commons.math3.linear.LUDecomposition
import org.apache.commons.math3.linear.QRDecomposition
import org.apache.commons.math3.linear.RealMatrix

object Matrix {
  
  def fromRowMajor(values:Array[Array[Double]]) : Matrix = 
    new Matrix(new Array2DRowRealMatrix(values))
  def fromRowMajor(values:Traversable[Traversable[Double]]) : Matrix = 
    fromRowMajor(values.map {_.toArray} toArray)
  def fromColumnMajor(values:Array[Array[Double]]) : Matrix = {
    val tmp = new Array2DRowRealMatrix(values)
    new Matrix(tmp.transpose)
  }
  def fromColumnMajor(values:Traversable[Traversable[Double]]) : Matrix =
    fromColumnMajor(values.map {_.toArray} toArray)

  def identity(size:Int) = {
    val vals = Array.fill(size, size)(0.0)
    for(i <- 0 until size) {
      vals(i)(i) = 1.0
    }
    Matrix.fromRowMajor(vals)
  }

  def random(rows:Int, cols:Int) = {
    val vals = Array.tabulate(rows, cols) {(_,_) => math.random}
    Matrix.fromRowMajor(vals)
  }
}

class Matrix(val proxy:RealMatrix) {

  //def this(values:Array[Array[Double]]) = this(new Array2DRowRealMatrix(values))
  //def this(values:Traversable[Traversable[Double]]) = this(values.map(_.toArray).toArray)

  def apply(row:Int, col:Int) : Double = proxy.getEntry(row, col)

  def set(r:Int, c:Int, v:Double) = proxy.setEntry(r, c, v)

  def column(c:Int) : Vector = new Vector(proxy.getColumn(c))
  def row(r:Int) : Vector = new Vector(proxy.getRow(r))

  def dot(m:Matrix) = {
    new Matrix(proxy.multiply(m.proxy))
  }

  def dot(v:Vector) : Vector = {
    new Vector(proxy.operate(v.proxy))
  }

  def +(v:Double) = new Matrix(proxy.scalarAdd(v))
  def -(v:Double) = new Matrix(proxy.scalarAdd(-v))
  def *(v:Double) = new Matrix(proxy.scalarMultiply(v))
  def /(v:Double) = new Matrix(proxy.scalarMultiply(1.0/v))

  def min : Double = {
    var v = proxy.getEntry(0,0)
    for(r <- 0 until rows) {
      for(c <- 0 until columns) {
        val v2 = proxy.getEntry(r,c)
        if(v2 < v) v = v2
      }
    }
    v
  }

  def max : Double = {
    var v = proxy.getEntry(0,0)
    for(r <- 0 until rows) {
      for(c <- 0 until columns) {
        val v2 = proxy.getEntry(r,c)
        if(v2 > v) v = v2
      }
    }
    v
  }

  def sum : Double = {
    var s = 0.0
    for(r <- 0 until rows) {
      for(c <- 0 until columns) {
        s += proxy.getEntry(r,c)
      }
    }
    s
  }

  def chol : (Matrix,Matrix) = {
    val tmp = new CholeskyDecomposition(proxy, 
      CholeskyDecomposition.DEFAULT_ABSOLUTE_POSITIVITY_THRESHOLD, 1e-9)
    (new Matrix(tmp.getL), new Matrix(tmp.getLT))
  }

  // TODO: automatically pick the best solver
  def inverse : Matrix = new Matrix(new QRDecomposition(proxy).getSolver.getInverse)

  def det : Double = new LUDecomposition(proxy).getDeterminant

  def rows = proxy.getRowDimension
  def columns = proxy.getColumnDimension

  def mapAll(f:Double=>Double) : Matrix = {
    Matrix.fromRowMajor(proxy.getData.map {_.map(f)})
  }

  def mapCols(f:Vector=>Vector) : Matrix = {
    val outMtx = proxy.copy
    (0 until columns).foreach {i =>
      val col = outMtx.getColumn(i)
      outMtx.setColumn(i, f(new Vector(col)).toArray)
    }
    new Matrix(outMtx)
  }
  def mapCols(f:Vector=>Double) : Vector = {
    val outVect = Array.fill(columns)(0.0)
    (0 until columns).foreach {i =>
      val col = proxy.getColumn(i)
      outVect(i) = f(new Vector(col))
    }
    new Vector(outVect)
  }
  def mapRows(f:Vector=>Vector) : Matrix = {
    val outMtx = proxy.copy
    (0 until rows).foreach {i =>
      val row = outMtx.getRow(i)
      outMtx.setRow(i, f(new Vector(row)).toArray)
    }
    new Matrix(outMtx)
  }
  def mapRows(f:Vector=>Double) : Vector = {
    val outVect = Array.fill(rows)(0.0)
    (0 until rows).foreach {i =>
      val row = proxy.getRow(i)
      outVect(i) = f(new Vector(row))
    }
    new Vector(outVect)
  }

  def toColumnMajorList : List[List[Double]] = 
    toColumnMajorArray.map {_.toList} toList
  def toRowMajorList : List[List[Double]] = 
    toRowMajorArray.map {_.toList} toList

  def toColumnMajorArray : Array[Array[Double]] = 
    proxy.transpose.getData
  def toRowMajorArray : Array[Array[Double]] = 
    proxy.getData

  override def toString : String = {
    (0 until rows).map({r =>
      "|" + 
      (0 until columns).map {c => 
        apply(r, c).toString
      }.reduceLeft(_ + " " + _) + 
      "|"
    }).reduceLeft(_ + "\n" + _)
  }
}

