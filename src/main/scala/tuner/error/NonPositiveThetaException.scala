package tuner.error

class NonPositiveThetaException(thetas:breeze.linalg.DenseVector[Double])
  extends Exception("thetas must be positive " + thetas.toString)

