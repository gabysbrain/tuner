package tuner.test

import tuner.Table

import breeze.linalg.DenseMatrix

object Util {

  def resource(file:String, exec:Boolean=false) : String = {
    val r = getClass.getResource(file)
    if(r == null)
      throw new java.io.IOException(s"resource '${file}' not found")

    if(exec) {
      val f = new java.io.File(r.getPath)
      f.setExecutable(true)
    }
    r.getPath.toString
  }

  def isLhc(mtx:DenseMatrix[Double]) : Boolean = {
    val indexMtx = mtx map {x => math.floor(x * mtx.rows).toInt}
    // All columns must be a permutation of 0 -> mtx.rows
    (0 until mtx.cols) forall {c =>
      indexMtx(::,c).data.toSet == (0 until mtx.rows).toSet
    }
  }

  def isLhc(tbl:Table) : Boolean = {
    val fields = tbl.fieldNames
    val mtx = DenseMatrix.zeros[Double](tbl.numRows, tbl.numFields)
    // Do this slowly but less error prone
    for(r <- 0 until tbl.numRows) {
      val tpl = tbl.tuple(r)
      for(c <- 0 until fields.length) {
        mtx(r, c) = tpl(fields(c))
      }
    }
    isLhc(mtx)
  }

}
