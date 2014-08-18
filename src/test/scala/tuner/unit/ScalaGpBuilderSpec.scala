package tuner.test.unit

import org.scalatest._
import org.scalatest.Matchers._

import tuner.gp.ScalaGpBuilder

import breeze.linalg._
import breeze.numerics._

class ScalaGpBuilderSpec extends WordSpec {

  "The minimum distance function" should {
    "given a simple 2x2 matrix" when {
      val m = DenseMatrix((1.0, 0.0), (2.0, 0.0))
      "return a distance of 1" in {
        ScalaGpBuilder.minDist(m) should be (1.0)
      }
    }
  }
}

