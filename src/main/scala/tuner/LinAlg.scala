package tuner

object LinAlg {

  def dotProd(M:Array[Array[Double]], v:Array[Double]) : Array[Double] = {
    val output = new Array[Double](M.size)
    for(i <- 0 until output.size) {
      output(i) = dotProd(M(i), v)
    }
    output
  }

  def dotProd(v1:Array[Double], v2:Array[Double]) : Double = {
    if(v1.size != v2.size)
      throw new Exception("bad size: " + v1.size + " " + v2.size)

    var sum:Double = 0
    for(i <- 0 until v1.size) {
      sum += v1(i) * v2(i)
    }
    sum
  }

  def ones(n:Int) : Array[Double] = {
    val tmp = new Array[Double](n)
    for(i <- 0 until n)
      tmp(i) = 1
    tmp
  }

  def simpsonsRule(f:Double => Double, min:Double, max:Double) : Double = {
    val v1 = (max - min) / 6.0
    /*
    val fmin = f(min)
    val favg = f((min+max)/2.0)
    val fmax = f(max)
    println("mn: " + min + " avg: " + (min+max)/2.0 + " mx: " + max)
    println("mn: " + fmin + " avg: " + favg + " mx: " + fmax)
    */
    val v2 = f(min) + 4.0 * f((min+max)/2.0) + f(max)
    v1 * v2
  }
}

