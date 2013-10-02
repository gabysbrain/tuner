package tuner.gp

import breeze.linalg._
import breeze.numerics._
import breeze.optimize._

import tuner.Table

/**
 * Build the Gaussian Process model using the numberz package
 */
object ScalaGpBuilder extends GpBuilder {


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

    val locs = DenseMatrix.zeros[Double](data.numRows, paramFields.length)
    (0 until data.numRows).foreach { r =>
      val tpl = data.tuple(r)
      paramFields.zipWithIndex.foreach {case(f, c) => 
        locs.update(r, c, tpl(f).toDouble)
      }
    }
    val resps = DenseVector(data map {tpl => tpl(responseField).toDouble} toArray)

    val (minNegLogL, mean, sig2, rInv, thetas, alphas) = findParams(locs, resps)
    new GpModel(thetas, alphas, mean, sig2, locs, resps, rInv, 
                paramFields, responseField, errorField)
  }

  def findParams(samples:DenseMatrix[Double], responses:DenseVector[Double]) 
      : (Double, Double, Double, DenseMatrix[Double], DenseVector[Double], DenseVector[Double]) = {
    
    // default to e^(-x^2) for now for correlation
    val alphas = DenseVector.fill(samples.cols){2.0}

    // need to do error checking on the minimization
    // find the negative log liklihood
    def optimFunc(x:DenseVector[Double]) = {
      // force the thetas to be positive
      val xx = x.map {math.exp(_)}
      -logLikelihood(samples, responses, xx, alphas)._1
    }
    val f = new ApproximateGradientFunction(optimFunc)

    // so in the spirit of the mlegp package do 
    // 5 optimizations with random restart 
    var minLL = Double.MaxValue
    var minPt:DenseVector[Double] = null
    for(i <- 0 until 5) {
      val optim = new LBFGS[DenseVector[Double]](maxIter=100, m=3)
      //val optim = new breeze.optimize.StochasticGradientDescent.SimpleSGD[DenseVector[Double]]
      //val optim = new breeze.optimize.OWLQN[DenseVector[Double]](maxIter=100, m=3)
      val start = DenseVector.rand(samples.cols)
      val pt = optim.minimize(f, start)
      val ll = optimFunc(pt)
      /*
      println("optimization #" + i 
           + " with log likelihood " + ll 
           + " at point " + pt.map(math.exp(_))
           + " starting at " + start.map(math.exp(_)))
      */
      if(ll <= minLL) {
        minLL = ll
        minPt = pt
      }
    }

    /*
    println("minimum found" 
         + " with log likelihood " + minLL
         + " at point " + minPt.map(math.exp(_)))
    */
    // One more time to get the final versions of the values
    val thetas = minPt map {math.exp(_)}
    val (ll, mu, sig2, rInv) = logLikelihood(samples, responses, 
                                             thetas, alphas)
    (ll, mu, sig2, rInv, thetas, alphas)
  }

  /**
   * Computes the negative log-likelihood of the theta and alpha parameters
   * 
   * Returns the log-likelihood, the calibrated mean, std-deviation, 
   * and inverse correlation matrix
   */
  def logLikelihood(samples:DenseMatrix[Double], 
                    responses:DenseVector[Double], 
                    thetas:DenseVector[Double], 
                    alphas:DenseVector[Double]) 
        : (Double, Double, Double, DenseMatrix[Double]) = {
    
    // check arguments
    if(thetas.min < 0.0)
      throw new tuner.error.NonPositiveThetaException(thetas)

    val n = samples.rows.toDouble
    val ones = DenseVector.ones[Double](samples.rows)

    val r = corrMatrix(samples, thetas, alphas)
    val rDet = breeze.linalg.det(r)
    if(rDet > 0.0) {
      val rInv = breeze.linalg.inv(r)
      // Using a constant mean
      val mean = (ones dot (rInv * responses)) / (ones dot (rInv * ones))
      val devs = responses - mean
      val sigTop = devs dot (rInv * devs) // so we only compute this once
      val sig2 = sigTop / n

      val logL = -0.5 * (n * (math.log(2*math.Pi) + math.log(sig2)) 
                         + math.log(rDet) 
                         + (sigTop/sig2))

      //println("logL: " + logL)
      (logL, mean, sig2, rInv)
    } else {
      (Double.MaxValue, 0.0, 0.0, DenseMatrix.eye[Double](samples.rows))
    }

  }

  def corrMatrix(samples:DenseMatrix[Double], 
                 thetas:DenseVector[Double], 
                 alphas:DenseVector[Double]) = {
    val mtx = DenseMatrix.eye[Double](samples.rows)
    for(r1 <- 0 until samples.rows) {
      for(r2 <- 0 until samples.rows) {
        if(r1 != r2) { // don't need to compute corr for the same point
          val corr = corrFunction(samples(r1,::).toDenseVector, 
                                  samples(r2,::).toDenseVector, 
                                  thetas, alphas)
          mtx(r1, r2) = corr
        }
      }
    }
    mtx
  }

  def corrFunction(p1:DenseVector[Double], p2:DenseVector[Double], 
                   thetas:DenseVector[Double], alphas:DenseVector[Double]) = {
    var sum = 0.0
    for(d <- 0 until p1.length) {
      sum += thetas(d) * math.pow(math.abs(p1(d) - p2(d)), alphas(d))
    }
    math.exp(-sum)
  }

}

