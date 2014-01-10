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

  val mtx = DenseMatrix((v1,  v5,  v9, v13), 
                        (v2,  v6, v10, v14), 
                        (v3,  v7, v11, v15), 
                        (v4,  v8, v12, v16))
  

  def *(m:Matrix4) : Matrix4 = {
    var tmp = mtx * m.mtx
    Matrix4(tmp(0, 0), tmp(0, 1), tmp(0, 2), tmp(0, 3),
            tmp(1, 0), tmp(1, 1), tmp(1, 2), tmp(1, 3),
            tmp(2, 0), tmp(2, 1), tmp(2, 2), tmp(2, 3),
            tmp(3, 0), tmp(3, 1), tmp(3, 2), tmp(3, 3))
  }

  //def toArray = toArray2
  def toOpenGl = {
    /*
    println(Array(mtx(0, 0), mtx(1, 0), mtx(2, 0), mtx(3, 0),
          mtx(0, 1), mtx(1, 1), mtx(2, 1), mtx(3, 1),
          mtx(0, 2), mtx(1, 2), mtx(2, 2), mtx(3, 2),
          mtx(0, 3), mtx(1, 3), mtx(2, 3), mtx(3, 3)).mkString(" "))
    */
    Array(mtx(0, 0), mtx(1, 0), mtx(2, 0), mtx(3, 0),
          mtx(0, 1), mtx(1, 1), mtx(2, 1), mtx(3, 1),
          mtx(0, 2), mtx(1, 2), mtx(2, 2), mtx(3, 2),
          mtx(0, 3), mtx(1, 3), mtx(2, 3), mtx(3, 3))
  }

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

