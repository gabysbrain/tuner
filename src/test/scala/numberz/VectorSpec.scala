package numberz.test

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Properties

import numberz.Vector

object VectorGen {
  def vectorType(size:Int) : Gen[Vector] = for {
    vals <- Gen.listOfN(size, Arbitrary.arbitrary[Double])
  } yield {
    Vector(vals)
  }

  def positiveVectorType(size:Int) : Gen[Vector] = for {
    vals <- Gen.listOfN(size, Gen.choose(0.0, Double.MaxValue) suchThat {_>0.0})
  } yield {
    Vector(vals)
  }


  val genVector = Gen.sized {n => vectorType(n)}
}

object VectorSpec extends Properties("Vector") {
  
  import VectorGen._

  property("length") = Prop.forAll(genVector) {v:Vector =>
    v.length == v.toArray.length
  }

}

