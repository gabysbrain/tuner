package numberz

import scala.collection.IndexedSeq
import scala.collection.IndexedSeqOptimized

import cern.colt.function.tdouble.DoubleFunction
import cern.colt.matrix.tdouble.DoubleMatrix1D
import cern.colt.matrix.tdouble.DoubleMatrix2D
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra

object Matrix {
  
  def apply(values:Array[Array[Double]]) = new Matrix(values)
  def apply(values:Traversable[Traversable[Double]]) = new Matrix(values)
}

class Matrix(val proxy:DoubleMatrix2D) {

  def this(values:Array[Array[Double]]) = this(new DenseDoubleMatrix2D(values))
  def this(values:Traversable[Traversable[Double]]) = this(values.map(_.toArray).toArray)

  def apply(col:Int) : Vector = column(col)

  def column(col:Int) : Vector = new Vector(proxy.viewColumn(col))
  def row(r:Int) : Vector = new Vector(proxy.viewRow(r))

  def dot(v:Vector) = {
    new Vector(algebra.mult(proxy, v.proxy))
  }

  def dot(m:Matrix) = {
    new Matrix(algebra.mult(proxy, m.proxy))
  }

  def *(v:Double) = this mapAll (_ * v)

  def chol : (Matrix,Matrix) = {
    val tmp = algebra.chol(proxy)
    (new Matrix(tmp.getL), new Matrix(tmp.getLtranspose))
  }

  def size = length
  def length : Int = proxy.size toInt

  def mapAll(f:Double=>Double) : Matrix = {
    val funcObj = new DoubleFunction {
      def apply(v:Double) : Double = f(v)
    }
    val outMtx = proxy.copy
    outMtx.assign(funcObj)
    new Matrix(outMtx)
  }

  def mapCols(f:Vector=>Vector) : Matrix = {
    val outMtx = proxy.copy
    (0 to outMtx.columns).foreach {i =>
      val col = outMtx.viewColumn(i)
      col.assign(f(new Vector(col)).proxy)
    }
    new Matrix(outMtx)
  }
  def mapCols(f:Vector=>Double) : Vector = {
    val outVect = new DenseDoubleMatrix1D(proxy.columns)
    (0 to proxy.columns).foreach {i =>
      val col = proxy.viewColumn(i)
      outVect.setQuick(i, f(new Vector(col)))
    }
    new Vector(outVect)
  }
  def mapRows(f:Vector=>Vector) : Matrix = {
    val outMtx = proxy.copy
    (0 to outMtx.rows).foreach {i =>
      val row = outMtx.viewRow(i)
      row.assign(f(new Vector(row)).proxy)
    }
    new Matrix(outMtx)
  }
  def mapRows(f:Vector=>Double) : Vector = {
    val outVect = new DenseDoubleMatrix1D(proxy.rows)
    (0 to proxy.rows).foreach {i =>
      val row = proxy.viewRow(i)
      outVect.setQuick(i, f(new Vector(row)))
    }
    new Vector(outVect)
  }

  def toList : List[List[Double]] = proxy.toArray.map {_.toList} toList

  protected def algebra = proxy match {
    case _:DenseDoubleMatrix2D => DenseDoubleAlgebra.DEFAULT
  }
}

