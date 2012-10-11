package numberz.test

import org.scalacheck._

import numberz.Matrix

object MatrixGen {
  val identityMatrixGen = Gen.sized {n => Matrix.identity(n+1)}

}

object MatrixSpec extends Properties("Matrix") {
  import MatrixGen._

  property("scalar multiplication") = 
    Prop.forAll(identityMatrixGen, 
                Arbitrary.arbitrary[Double]) {case (m:Matrix, v:Double) =>
    val res = m * v
    var allOk = true
    for(r <- 0 until m.rows) {
      for(c <- 0 until m.columns) {
        allOk = allOk && (m(r,c) * v == res(r,c))
      }
    }
    allOk
  }
}

