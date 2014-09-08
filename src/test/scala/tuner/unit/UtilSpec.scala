package tuner.test.unit

import org.scalatest._
import org.scalatest.Matchers._

import tuner.util.Util

class UtilSpec extends WordSpec {

  "The maxIndex function" must {
    "return index 2" when {
      "given an array with the max in index 2" in {
        var a = Array(0.0, -2.0, 45.0, 32.1)
        Util.maxIndex(a) should equal (2)
      }
    }
  }

}

