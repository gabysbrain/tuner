package tuner.error

class NonPositiveThetaException(thetas:numberz.Vector)
  extends Exception("thetas must be positive " + thetas.toString)

