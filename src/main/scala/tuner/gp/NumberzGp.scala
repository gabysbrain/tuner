package tuner.gp

import tuner.Table

import numberz.Matrix
import numberz.Minimizer
import numberz.Vector

/**
 * Build the Gaussian Process model using the numberz package
 */
class NumberzGp extends GpBuilder {


  def buildModel(dataFile:String,
                 paramFields:List[String],
                 responseField:String,
                 errorField:String) : GpModel = {
    buildModel(Table.fromCsv(dataFile), 
               paramFields, responseField, errorField)
  }

  def buildModel(data:Table,
                 paramFields:List[String],
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
    val alphas = Vector.fill(samples.columns)(2.0)

    // need to do error checking on the minimization
    // find the negative log lilihood
    def optimFunc(x:Array[Double]) = 
      logLikelihood(samples, responses, Vector(x), alphas)._1
    val bounds = Array.fill(samples.columns)((1e-15, Double.MaxValue))
    val minPt = Minimizer.bobyqa(samples.columns, optimFunc, bounds)

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
    
    // check arguments
    if(thetas.exists(_ < 0.0))
      throw new tuner.error.NonPositiveThetaException(thetas)

    val n = samples.rows.toDouble
    val ones = Vector.ones(samples.rows)

    val r = corrMatrix(samples, thetas, alphas)
    val rDet = r.det
    if(rDet > 0.0) {
      val rInv = r.inverse
      // Using a constant mean
      val mean = (ones dot (rInv dot responses)) / (ones dot (rInv dot ones))
      val devs = responses - mean
      val sigTop = (devs dot (rInv dot devs))  // so we only compute this once
      val sig2 = sigTop / n

      val logL = -0.5 * (n * (math.log(2*math.Pi) + math.log(sig2)) 
                         + math.log(rDet) 
                         + (sigTop/sig2))

      //println("logL: " + logL)
      (logL, mean, sig2, rInv)
    } else {
      (Double.MinValue, 0.0, 0.0, Matrix.identity(samples.rows))
    }

  }

  def corrMatrix(samples:Matrix, thetas:Vector, alphas:Vector) = {
    val mtx = Matrix.identity(samples.rows)
    for(r1 <- 0 until samples.rows) {
      for(r2 <- 0 until samples.rows) {
        if(r1 != r2) { // don't need to compute corr for the same point
          val corr = corrFunction(samples.row(r1), samples.row(r2), 
                                  thetas, alphas)
          mtx.set(r1, r2, corr)
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

