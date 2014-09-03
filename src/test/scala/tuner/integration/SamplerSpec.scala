package tuner.test.integration

import tuner.Sampler
import tuner.DimRanges

import tuner.test.Util._

class SamplerSpec extends IntegrationTest {
  
  "A Sampler object" should {
    "a lhc is selected" should {
      "return a proper lhc" in {
        val dr = new DimRanges(Map("x1" -> (0f, 1f), 
                                   "x2" -> (0f, 1f),
                                   "x3" -> (0f, 1f)))
        val lhc = Sampler.lhc(dr, 10)
        isLhc(lhc) must beTrue
      }
    }

    "sampling 1 point" should {
      "return a table with 1 row" in {
        val dr = new DimRanges(Map("x1" -> (0f, 1f), 
                                   "x2" -> (0f, 1f),
                                   "x3" -> (0f, 1f)))
        val lhc = Sampler.lhc(dr, 1)
        lhc.numRows must_== 1
      }
    }

    "sampling 0 points" should {
      "return an empty table" in {
        val dr = new DimRanges(Map("x1" -> (0f, 1f), 
                                   "x2" -> (0f, 1f),
                                   "x3" -> (0f, 1f)))
        val lhc = Sampler.lhc(dr, 0)
        lhc must be empty
      }
    }
  }
}

