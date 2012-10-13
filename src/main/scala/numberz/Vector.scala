package numberz

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

object Vector { 
  
  def apply(values:Array[Double]) = new Vector(values)
  def apply(values:Traversable[Double]) = new Vector(values)

  def zeros(size:Int) = new Vector(Array.fill(size)(0.0))
  def ones(size:Int) = new Vector(Array.fill(size)(1.0))
  def fill(size:Int)(x:Double) = new Vector(Array.fill(size)(x))
}

class Vector(val proxy:RealVector) {

  def this(values:Array[Double]) = this(new ArrayRealVector(values))
  def this(values:Traversable[Double]) = this(values.toArray)

  def apply(idx:Int) = proxy.getEntry(idx)

  def +(v:Double) = new Vector(proxy.mapAdd(v))
  def -(v:Double) = new Vector(proxy.mapSubtract(v))
  def *(v:Double) = new Vector(proxy.mapMultiply(v))
  def *(v:Vector) = new Vector(toArray.zip(v.toArray).map {x=>x._1*x._2})
  def /(v:Double) = new Vector(proxy.mapDivide(v))

  def dot(v2:Vector) : Double = proxy.dotProduct(v2.proxy)
  def outer(v2:Vector) : Matrix = new Matrix(proxy.outerProduct(v2.proxy))

  def min = toArray.min
  def max = toArray.max
  def sum = toArray.sum

  def map(f:Double=>Double) : Vector = {
    new Vector(toArray.map(f))
  }
  def foldLeft[B](z:B)(f:(B,Double)=>B) = toArray.foldLeft(z)(f)
  def forall(f:Double=>Boolean) = foldLeft(true) {(cur:Boolean,x:Double) =>
    cur && f(x)
  }
  def exists(f:Double=>Boolean) = foldLeft(false) {(cur:Boolean,x:Double) =>
    cur || f(x)
  }

  def length = proxy.getDimension
  def size = length

  def toList : List[Double] = toArray.toList
  def toArray : Array[Double] = proxy.toArray

  override def toString : String = 
    "(" + toList.map(_.toString).reduceLeft(_ + ", " + _) + ")"
}

