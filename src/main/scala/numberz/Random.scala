package numberz

import org.apache.commons.math3.random.CorrelatedRandomVectorGenerator
import org.apache.commons.math3.random.GaussianRandomGenerator
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.UncorrelatedRandomVectorGenerator

object Random {

  val uniformGen = new MersenneTwister
  val normalGen = new GaussianRandomGenerator(uniformGen)

  def uniform(min:Double, max:Double) : Double = {
    val range = max - min
    min + uniformGen.nextDouble * range
  }

  def normal(size:Int, mean:Double, sig2:Double) : Vector = {
    val rng = new UncorrelatedRandomVectorGenerator(
      Array.fill(size)(mean), Array.fill(size)(math.sqrt(sig2)), normalGen)
    Vector(rng.nextVector)
  }

  def normal(mean:Double, cov:Matrix) : Vector = {
    val rng = new CorrelatedRandomVectorGenerator(
      Array.fill(cov.rows)(mean), cov.proxy, 1e-12*cov.norm, normalGen)
    Vector(rng.nextVector)
  }

}

