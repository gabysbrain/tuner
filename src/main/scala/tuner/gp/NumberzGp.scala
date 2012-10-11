package tuner.gp

import tuner.Table

import numberz.Matrix
import numberz.Optim
import numberz.Vector

/**
 * Build the Gaussian Process model using the numberz package
 */
class NumberzGp(designFile:String) extends GpBuilder(designFile) {

  val data = Table.fromCsv(designFile)

  def buildModel(paramFields:List[String],
                 responseField:String,
                 errorField:String) : GpModel = {

    val locs = Matrix.fromRowMajor(
      data map {tpl => paramFields.map {f => tpl(f).toDouble}}
    )
    val resps = Vector(data map {tpl => tpl(responseField).toDouble})

    val (minNegLogL, mean, sig2, rInv, thetas, alphas) = findParams(locs, resps)
    new GpModel(thetas, alphas, mean, sig2, locs, resps, rInv, 
                paramFields, responseField, errorField)
  }

  def findParams(samples:Matrix, responses:Vector) 
      : (Double, Double, Double, Matrix, Vector, Vector) = {
    
    // default to e^(-x^2) for now for correlation
    val alphas = Vector(List.fill(samples.columns)(2.0))

    // need to do error checking on the minimization
    def optimFunc(x:Array[Double]) = 
      logLikelihood(samples, responses, Vector(x), alphas)._1
    val minPt = Optim.nelderMead(samples.columns, optimFunc)

    // One more time to get the final versions of the values
    val thetas = Vector(minPt)
    val (ll, mu, sig2, rInv) = logLikelihood(samples, responses, 
                                             thetas, alphas)
    (ll, mu, sig2, rInv, thetas, alphas)
  }

  /**
   * Computes the log-likelihood of the theta and alpha parameters
   * 
   * Returns the log-likelihood, the calibrated mean, std-deviation, 
   * and inverse correlation matrix
   */
  def logLikelihood(samples:Matrix, responses:Vector, 
                    thetas:Vector, alphas:Vector) 
        : (Double, Double, Double, Matrix) = {
    
    val n = samples.rows.toDouble
    val ones = Vector.ones(samples.rows)

    val r = corrMatrix(samples, thetas, alphas)
    val rInv = r.inverse
    // Using a constant mean
    val mean = (ones dot (rInv dot responses)) / (ones dot (rInv dot ones))
    val devs = responses - mean
    val sigTop = (devs dot (rInv dot devs))  // so we only compute this once
    val sig2 = sigTop / n

    val logL = (-n * (math.log(2*math.Pi) + math.log(sig2)) 
                  + math.log(r.det)) / 2.0
             - (sigTop / (2*sig2))

    (logL, mean, sig2, rInv)
  }

  def corrMatrix(samples:Matrix, thetas:Vector, alphas:Vector) = {
    val mtx = Matrix.identity(samples.rows)
    for(r <- 0 until samples.rows) {
      for(c <- 0 until samples.columns) {
        if(r != c) { // don't need to compute corr for the same point
          val corr = corrFunction(samples.row(r), samples.row(c), 
                                  thetas, alphas)
          mtx.set(r, c, corr)
        }
      }
    }
    mtx
  }

  def corrFunction(p1:Vector, p2:Vector, thetas:Vector, alphas:Vector) = {
    var sum = 0.0
    for(d <- 0 until p1.length) {
      sum += thetas(d) * math.pow(math.abs(p1(d) - p2(d)), alphas(d))
    }
    math.exp(-sum)
  }

}

