package tuner.test.unit

import org.scalatest._

import tuner.LHS

import tuner.test.Util._

class LHSSpec extends WordSpec {

  "A maximin latin hypercube" should {
    "given a particular dimensionality" when {
      val lhc = LHS.maximin(4, 3)
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

