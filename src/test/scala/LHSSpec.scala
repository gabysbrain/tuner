package test.tuner

import org.scalatest._

import breeze.linalg.DenseMatrix
import tuner.LHS

class LHSSpec extends WordSpec {

  def isLhc(mtx:DenseMatrix[Double]) : Boolean = {
    val indexMtx = mtx map {x => math.floor(x * mtx.rows).toInt}
    // All columns must be a permutation of 0 -> mtx.rows
    (0 until mtx.cols) forall {c =>
      indexMtx(::,c).data.toSet == (0 until mtx.rows).toSet
    }
  }

  "A random latin hypercube" should {
    "given a particular dimensionality" when {
      val lhc = LHS.random(4, 3)
      "return a matrix of the correct size" in {
        assert(lhc.rows == 4)
        assert(lhc.cols == 3)
      }
      "have all values between 0 and 1" in {
        assert(lhc.map {x => x >=0 && x <= 1} all)
      }
      "have only 1 value per row and column" in {
        assert(isLhc(lhc))
      }
    }
  }

}

