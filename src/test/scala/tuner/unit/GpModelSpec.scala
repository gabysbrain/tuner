package tuner.test.unit

import org.scalatest._
import org.scalatest.Matchers._

import tuner.Table
import tuner.gui.util.Histogram

class GpModelSpec extends WordSpec {
  "estimating a point" when {
    "estimating at a sample point" must {
      "have an error of 0" in (pending)
      "return the value of the sample point" in (pending)
    }

    "estimating away from a sample point" must {
      "have an error of >0" in (pending)
      "have an error of <=1" in (pending)
    }
  }
}
