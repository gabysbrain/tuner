package tuner.gui.util

import breeze.linalg._
import breeze.numerics._

object Matrix4 {
  def apply( v1:Float,  v5:Float,  v9:Float, v13:Float, 
             v2:Float,  v6:Float, v10:Float, v14:Float, 
             v3:Float,  v7:Float, v11:Float, v15:Float, 
             v4:Float,  v8:Float, v12:Float, v16:Float) = {
    new Matrix4(v1, v5,  v9, v13,
                v2, v6, v10, v14, 
                v3, v7, v11, v15, 
                v4, v8, v12, v16)
  }

  def identity = new Matrix4(
    1, 0, 0, 0,
    0, 1, 0, 0,
    0, 0, 1, 0,
    0, 0, 0, 1
  )

  def scale(x:Float, y:Float, z:Float) = new Matrix4(
    x, 0, 0, 0,
    0, y, 0, 0,
    0, 0, z, 0,
    0, 0, 0, 1
  )

  def translate(x:Float, y:Float, z:Float) = new Matrix4(
    1, 0, 0, x,
    0, 1, 0, y,
    0, 0, 1, z,
    0, 0, 0, 1
  )

}

class Matrix4( v1:Float,  v5:Float,  v9:Float, v13:Float, 
               v2:Float,  v6:Float, v10:Float, v14:Float, 
               v3:Float,  v7:Float, v11:Float, v15:Float, 
               v4:Float,  v8:Float, v12:Float, v16:Float) {

  val mtx = new DenseMatrix[Float](4, 4, Array( v1,  v5,  v9,  v13,
                                                v2,  v6, v10,  v14,
                                                v3,  v7, v11,  v15,
                                                v4,  v8, v12,  v16))
  

  /*
  def this(vv:Array[Float]) = 
    this( vv(0),  vv(4),  vv(8), vv(12), 
          vv(1),  vv(5),  vv(9), vv(13), 
          vv(2),  vv(6), vv(10), vv(14), 
          vv(3),  vv(7), vv(11), vv(15))
  */
  /*
  def this(mm:DenseMatrix[Float]) =
    this(mm(0, 0), mm(0, 1), mm(0, 2), mm(0, 3),
         mm(1, 0), mm(1, 1), mm(1, 2), mm(1, 3),
         mm(2, 0), mm(2, 1), mm(2, 2), mm(2, 3),
         mm(3, 0), mm(3, 1), mm(3, 2), mm(3, 3))
  */

  def *(m:Matrix4) : Matrix4 = {
    var tmp = mtx * m.mtx
    Matrix4(tmp(0, 0), tmp(0, 1), tmp(0, 2), tmp(0, 3),
            tmp(1, 0), tmp(1, 1), tmp(1, 2), tmp(1, 3),
            tmp(2, 0), tmp(2, 1), tmp(2, 2), tmp(2, 3),
            tmp(3, 0), tmp(3, 1), tmp(3, 2), tmp(3, 3))
  }

  //def toArray = toArray2
  def toOpenGl = mtx.data

  override def toString = {
    (0 until 4).map({r =>
      "|" + 
      (0 until 4).map {c => 
        mtx(r, c).toString
      }.reduceLeft(_ + " " + _) + 
      "|"
    }).reduceLeft(_ + "\n" + _)
  }

  def dot(m2:Matrix4) : Matrix4 = this * m2
}

