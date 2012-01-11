package tuner.gui.util

import Jama.Matrix

object Matrix4 {
  def apply( v1:Float,  v2:Float,  v3:Float,  v4:Float, 
             v5:Float,  v6:Float,  v7:Float,  v8:Float, 
             v9:Float, v10:Float, v11:Float, v12:Float, 
            v13:Float, v14:Float, v15:Float, v16:Float) = {
    new Matrix4(v1,  v2,  v3,  v4, 
                v5,  v6,  v7,  v8, 
                v9, v10, v11, v12, 
                v13, v14, v15, v16)
  }

  def identity = Matrix.identity(4, 4)

  def scale(x:Float, y:Float, z:Float) = new Matrix4(
    x, 0, 0, 0,
    0, y, 0, 0,
    0, 0, z, 0,
    0, 0, 0, 1
  )

  def translate(x:Float, y:Float, z:Float) = new Matrix4(
    0, 0, 0, x,
    0, 0, 0, y,
    0, 0, 0, z,
    0, 0, 0, 1
  )

  implicit def mtx2Matrix4(mm:Matrix) = new Matrix4(mm.getRowPackedCopy)
}

class Matrix4( v1:Float,  v2:Float,  v3:Float,  v4:Float, 
               v5:Float,  v6:Float,  v7:Float,  v8:Float, 
               v9:Float, v10:Float, v11:Float, v12:Float, 
              v13:Float, v14:Float, v15:Float, v16:Float)
  extends Matrix(Array(v1,  v2,  v3,  v4, 
                       v5,  v6,  v7,  v8, 
                       v9, v10, v11, v12, 
                       v13, v14, v15, v16).map(_.toDouble), 4) {

  def this(vv:Array[Double]) = 
    this( vv(0).toFloat,  vv(1).toFloat,  vv(2).toFloat,  vv(3).toFloat, 
          vv(4).toFloat,  vv(5).toFloat,  vv(6).toFloat,  vv(7).toFloat, 
          vv(8).toFloat,  vv(9).toFloat, vv(10).toFloat, vv(11).toFloat, 
         vv(12).toFloat, vv(13).toFloat, vv(14).toFloat, vv(15).toFloat)

  def *(m:Matrix4) : Matrix4 = times(m)

  def toArray = getRowPackedCopy.map(_.toFloat)
}

