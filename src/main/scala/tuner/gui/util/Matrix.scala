package tuner.gui.util

import org.jblas.FloatMatrix

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

  def identity = new Matrix4(FloatMatrix.eye(4).toArray)

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

  implicit def mtx2Matrix4(mm:FloatMatrix) = new Matrix4(mm.toArray)
}

class Matrix4( v1:Float,  v5:Float,  v9:Float, v13:Float, 
               v2:Float,  v6:Float, v10:Float, v14:Float, 
               v3:Float,  v7:Float, v11:Float, v15:Float, 
               v4:Float,  v8:Float, v12:Float, v16:Float)
  extends FloatMatrix(Array(Array(v1,  v5,  v9,  v13),
                            Array(v2,  v6, v10,  v14),
                            Array(v3,  v7, v11,  v15),
                            Array(v4,  v8, v12,  v16))) {
  

  def this(vv:Array[Float]) = 
    this( vv(0),  vv(4),  vv(8), vv(12), 
          vv(1),  vv(5),  vv(9), vv(13), 
          vv(2),  vv(6), vv(10), vv(14), 
          vv(3),  vv(7), vv(11), vv(15))

  def *(m:Matrix4) : Matrix4 = mmul(m)

  //def toArray = toArray2

  override def toString = {
    (0 until rows).map({r =>
      "|" + 
      (0 until columns).map {c => 
        get(r, c).toString
      }.reduceLeft(_ + " " + _) + 
      "|"
    }).reduceLeft(_ + "\n" + _)
  }

}

