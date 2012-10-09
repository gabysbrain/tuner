package numberz

import scala.collection.IndexedSeq
import scala.collection.IndexedSeqOptimized

import cern.colt.function.tdouble.DoubleFunction
import cern.colt.function.tdouble.DoubleDoubleFunction
import cern.colt.matrix.tdouble.DoubleMatrix1D
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra

object Vector { //extends TraversableFactory[Vector] {
  
  def apply(values:Array[Double]) = new Vector(values)
  def apply(values:Traversable[Double]) = new Vector(values)

  def ones(size:Int) = new numberz.Vector(List.fill(size)(1.0))
}

class Vector(val proxy:DoubleMatrix1D) {

  def this(values:Array[Double]) = this(new DenseDoubleMatrix1D(values))
  def this(values:Traversable[Double]) = this(values.toArray)

  def apply(i:Int) : Double = proxy.getQuick(i)

  def dot(v:Vector) : Double = algebra.mult(proxy, v.proxy)

  def *(v:Double) = this map (_ * v)
  def *(v:Vector) = {
    val mult = new DoubleDoubleFunction {
      def apply(x:Double, y:Double) : Double = x * y
    }
    val outVect = proxy.copy
    outVect.assign(v.proxy, mult)
    new Vector(outVect)
  }

  def /(v:Double) = this map (_ / v)

  def sum : Double = proxy.zSum

  def min : Double = proxy.getMinLocation()(0)
  def max : Double = proxy.getMaxLocation()(0)

  def size = length
  def length : Int = proxy.size toInt

  def map(f:Double=>Double) : Vector = {
    val funcObj = new DoubleFunction {
      def apply(v:Double) : Double = f(v)
    }
    val outVect = proxy.copy
    outVect.assign(funcObj)
    new Vector(outVect)
  }

  def toArray : Array[Double] = proxy.toArray()
  def toList : List[Double] = proxy.toArray().toList

  protected def algebra = proxy match {
    case _:DenseDoubleMatrix1D => DenseDoubleAlgebra.DEFAULT
  }
}

