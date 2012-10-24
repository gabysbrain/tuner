package numberz.test

import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.matchers.MustMatchers
import org.scalatest.prop.Checkers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import numberz.Random

class RandomSpec extends FunSuite 
                 with GeneratorDrivenPropertyChecks 
                 with MustMatchers {
  
  val rangeGen = for {
    x1 <- Arbitrary.arbitrary[Double]
    x2 <- Arbitrary.arbitrary[Double]
  } yield {
    if(x1 < x2) (x1, x2)
    else        (x2, x1)
  }

  test("random number generator must generate values in range") {
    forAll(rangeGen) {case (mn, mx) =>
      val num = Random.uniform(mn, mx)
      num must be >= mn
      num must be <= mx
    }
  }
}

