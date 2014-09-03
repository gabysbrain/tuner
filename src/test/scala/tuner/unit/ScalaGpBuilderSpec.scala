package tuner.test.unit

import tuner.gp.ScalaGpBuilder

import breeze.linalg._
import breeze.numerics._

class ScalaGpBuilderSpec extends UnitTest {

  "minDist when given a simple 2x2 matrix" should {
    val m = DenseMatrix((1.0, 0.0), (2.0, 0.0))
    "return a distance of 1" in {
      ScalaGpBuilder.minDist(m) must_== 1.0
    }
  }
}

