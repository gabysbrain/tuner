package tuner.gui.util

import numberz.Matrix

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

class Matrix4(val proxy:Matrix) {

  def this( v1:Float,  v5:Float,  v9:Float, v13:Float, 
            v2:Float,  v6:Float, v10:Float, v14:Float, 
            v3:Float,  v7:Float, v11:Float, v15:Float, 
            v4:Float,  v8:Float, v12:Float, v16:Float) = 
        this(Matrix.fromRowMajor(Array(
          Array( v1.toDouble, v5.toDouble,  v9.toDouble, v13.toDouble),
          Array( v2.toDouble, v6.toDouble, v10.toDouble, v14.toDouble),
          Array( v3.toDouble, v7.toDouble, v11.toDouble, v15.toDouble),
          Array( v4.toDouble, v8.toDouble, v12.toDouble, v16.toDouble))))

  def dot(m2:Matrix4) : Matrix4 = new Matrix4(proxy dot m2.proxy)
  def toArray : Array[Float] = proxy.toColumnMajorArray.flatten.map {_.toFloat} 
}

