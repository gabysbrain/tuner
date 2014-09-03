package tuner.test.unit

import tuner.LHS

import tuner.test.Util._

class LHSSpec extends UnitTest {

  "A maximin LHC" should {
    "When given a particular dimensionality" should {
      val lhc = LHS.maximin(4, 3)
      "return a matrix of the correct size" in {
        lhc.rows must_== 4
        lhc.cols must_== 3
      }
      "have all values between 0 and 1" in {
        lhc.valuesIterator must contain(beBetween(0.0, 1.0)).foreach
      }
      "have only 1 value per row and column" in {
        isLhc(lhc) must beTrue
      }
  
    }

    "if asked for one point" should {
      "return a single row matrix" in {
        val lhc = LHS.maximin(1, 3)
        lhc.rows must_== 1
        lhc.cols must_== 3
      }
    }
  }

  "A random LHC" should {
    "when given a particular dimensionality" should {
      val lhc = LHS.random(4, 3)
      "return a matrix of the correct size" in {
        lhc.rows must_== 4
        lhc.cols must_== 3
      }
      "have all values between 0 and 1" in {
        lhc.valuesIterator must contain(beBetween(0.0, 1.0)).foreach
      }
      "have only 1 value per row and column" in {
        isLhc(lhc) must beTrue
      }
    }
  
    "asked for one point" should {
      "return a single row matrix" in {
        val lhc = LHS.random(1, 3)
        lhc.rows must_== 1
        lhc.cols must_== 3
      }
    }
  }
}

