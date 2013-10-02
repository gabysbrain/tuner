package tuner

import breeze.linalg._

import scala.util.Random

object LHS {

  def random(n:Int, d:Int) : DenseMatrix[Double] = {
    val basePerm = (0 until n).toList map {_.toDouble}

    val mtx = DenseMatrix.rand(n, d) / n.toDouble
    // Handle each column separately
    for(c <- 0 until d) {
      val perm = new DenseVector[Double](Random.shuffle(basePerm) toArray)
      mtx(::,c) += perm * (1.0 / n.toDouble)
    }
    mtx
  }

}
