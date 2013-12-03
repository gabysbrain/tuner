package tuner.test.integration

import org.scalatest._
import org.scalatest.Matchers._

import tuner.Sampler
import tuner.DimRanges

import tuner.test.Util._

class SamplerSpec extends WordSpec {
  
  "A Sampler object" when {
    "a lhc is selected" must {
      "return a proper lhc" in {
        val dr = new DimRanges(Map("x1" -> (0f, 1f), 
                                   "x2" -> (0f, 1f),
                                   "x3" -> (0f, 1f)))
        val lhc = Sampler.lhc(dr, 10)
        assert(isLhc(lhc))
      }
    }
  }
}

