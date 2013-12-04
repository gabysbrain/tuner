package tuner

import breeze.linalg._

import scala.util.Random
import scala.collection.mutable.HashSet

object LHS {

  /**
   * Greedily finds the latin hypercube that maximizes the minimum
   * distance between sample points
   * 
   * This is based on the R lhs sampling package
   * (http://cran.r-project.org/web/packages/lhs/) and
   * http://www.csit.fsu.edu/~burkardt/m_src/ihs/ihs.m
   *
   * The algorithm works as follows:
   * 1. Pick a random cell for the first point
   * 1. Randomize the list of remaining cells 
   *    (this is the new set of points)
   * 2. For each candidate point compute the minimum distance to the
   *    chosen points
   * 3. Select the candidate point with the maximum minimum distance
   * 4. remove the cells of this point from the candidate list
   * 5. repeat 2-4 
   */
  def maximin(n:Int, d:Int) : DenseMatrix[Double] = {

    // Initialize the candidates
    val candidates = Array.fill(d) {
      new HashSet() ++ (0 until n) map {_.toDouble}
    }
    val result = DenseMatrix.zeros[Double](n, d)

    // First point is easy
    for(c <- 0 until d) {
      val xx = Random.shuffle(candidates(c) toList).head
      result.update(0, c, xx)
      candidates(c) -= xx
    }
    //println("cand len: " + candidates(0).size)

    // The middle points are more complicated
    for(r <- 1 until (n-1)) {
      // Generate all the candidate points
      val candPts = DenseMatrix.zeros[Double](n-r, d)
      val shuffled = candidates map {cand => Random.shuffle(cand.toList) toArray}
      for(c <- 0 until d) {
        candPts(::, c) := new DenseVector[Double](shuffled(c))
      }
      //println(candPts)
      //println("+++")

      // find the minimum distance from the chosen 
      // points to the candidate points
      val minDists = DenseVector.fill[Double](candPts.rows) {Double.MaxValue}
      for(cpRow <- 0 until candPts.rows) {
        for(ptRow <- 0 until r) {
          val diff = (result(ptRow, ::) - candPts(cpRow, ::)).toDenseVector
          val sqDist = diff dot diff
          if(sqDist < minDists(cpRow))
            minDists(cpRow) = sqDist
        }
      }

      var minIdx = -1
      var maxDist = Double.MinValue
      for(cpRow <- 0 until minDists.length) {
        if(minDists(cpRow) > maxDist) {
          minIdx = cpRow
          maxDist = minDists(cpRow)
        }
      }

      val minPt = candPts(minIdx, ::).toDenseVector
      result(r, ::) := minPt

      // remove the min pt from the candidates
      for(c <- 0 until d) {
        candidates(c) -= minPt(c)
      }
      //println("cand len: " + candidates(0).size)
    }

    // Last point is whatever's left
    if(n > 1) {
      for(c <- 0 until d) {
        val xx = candidates(c).head
        result.update(n-1, c, xx)
      }
    }

    //println(result)
    (result + DenseMatrix.rand(n, d)) / n.toDouble
  }

  def random(n:Int, d:Int) : DenseMatrix[Double] = {
    val basePerm = (0 until n).toList map {_.toDouble}

    // all numbers in the matrix are now in the first bin
    val mtx = DenseMatrix.rand(n, d) / n.toDouble

    // Handle each column separately
    for(c <- 0 until d) {
      val perm = new DenseVector[Double](Random.shuffle(basePerm) toArray)
      // move the random number into the correct bin
      mtx(::,c) :+= perm * (1.0 / n.toDouble)
    }
    mtx
  }
}
