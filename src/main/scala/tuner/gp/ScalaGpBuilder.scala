package tuner.gp

import breeze.linalg._
import breeze.numerics._
import breeze.optimize._

import scala.util.Try

import tuner.Table

/**
 * Build the Gaussian Process model using the breeze package
 */
object ScalaGpBuilder extends GpBuilder {


  def buildModel(dataFile:String,
                 paramFields:List[String],
                 responseField:String,
                 errorField:String) : Try[GpModel] = {
    buildModel(Table.fromCsv(dataFile), 
               paramFields, responseField, errorField)
  }

  def buildModel(data:Table,
                 paramFields:List[String],
                 responseField:String,
                 errorField:String) : Try[GpModel] = {

    val locs = DenseMatrix.zeros[Double](data.numRows, paramFields.length)
    (0 until data.numRows).foreach { r =>
      val tpl = data.tuple(r)
      paramFields.zipWithIndex.foreach {case(f, c) => 
        locs.update(r, c, tpl(f).toDouble)
      }
    }
    val resps = DenseVector(data map {tpl => tpl(responseField).toDouble} toArray)

    Try({
      val (minNegLogL, mean, sig2, rInv, thetas, alphas) = findParams(locs, resps)
      //println("here")
      new GpModel(thetas, alphas, mean, sig2, locs, resps, rInv, 
                  paramFields, responseField, errorField)
    })
  }

  def minDist(samples:DenseMatrix[Double]) : Double = {
    var md = Double.MaxValue
    for(r1 <- 0 until (samples.rows-1)) {
      val row1 = samples(r1, ::)
      for(r2 <- (r1+1) until samples.rows) {
        val row2 = samples(r2, ::)
        val dist:Double = {
          val diffs = row1-row2
          sum((diffs :* diffs).inner)
        }
        //println("r1 " + row1 + " r2 " + row2 + " d " + dist)
        if(dist < md) {md = dist}
      }
    }
    math.sqrt(md)
  }

  /**
    * find starting parameters for the GP optimization.
    *
    * This is based on the mlegp R function which finds the minimum
    * euclidean distance between the points, then sets the minimum/maximum 
    * initial parameters to -log(0.65)/mindist and -log(0.3)/mindist.  I'm
    * not entirely sure why this is but then we generate a list
    * of random numbers in this range.
    */
  def startParams(samples:DenseMatrix[Double], retries:Int=5) 
      : List[DenseVector[Double]] = {
    val md = minDist(samples)
    val minCorr = -math.log(0.65) / md
    val maxCorr = -math.log(0.30) / md
    //println("md " + md + " mn " + minCorr + " mx " + maxCorr)
    List.fill(retries) {
      DenseVector.rand(samples.cols) * (maxCorr - minCorr) + minCorr
      //DenseVector(0.50, 1.84, 3.54)
      //DenseVector(1.65, 6.30, 34.49)
    }
  }

  /**
    * Run an optimization to find the GP parameters given the design
    * samples and sampled responses.
    */
  def findParams(samples:DenseMatrix[Double], responses:DenseVector[Double], 
                 retries:Int = 5) 
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
    //  a number of optimizations with random restart 
    val results:List[Try[(Double,DenseVector[Double])]] = 
          startParams(samples, retries) map {start =>
      val optim = new LBFGS[DenseVector[Double]](maxIter=100, m=3)
      //val optim = new breeze.optimize.StochasticGradientDescent.SimpleSGD[DenseVector[Double]]
      //val optim = new breeze.optimize.OWLQN[DenseVector[Double]](maxIter=100, m=3)
      //val start = DenseVector.rand(samples.cols)
      //println("running optimization...")
      //println("start pos: " + start)
      Try({
        val pt = optim.minimize(f, start)
        /*
        if(results.iter == 0) {
          throw new tuner.error.GpBuildException("start and end points of optimization are equal")
        }
        if(pt == start) {
          throw new tuner.error.GpBuildException("start and end points of optimization are equal")
        }
        */
        //val ll = optimFunc(pt)
        (optimFunc(pt), pt)
        //println("optim result: " + pt + " " + results.value)
        //println("took " + results.iter + " iterations")
        //println("success? " + !results.searchFailed)
        //println("reason: " + results.convergedReason)
      })
    }
    //println("finished with optimizations")

    //println("here2")
    //println("all res " + results)
    val (minLL, minPt) = results.map {r => 
      r.getOrElse(Double.MaxValue, DenseVector.zeros[Double](samples.cols))
    } minBy {_._1}
    //println("here3")

    // If there's no result from any of the attempts 
    // then the calibration has failed
    if(minLL == Double.MaxValue) {

      //println("here4")
      throw new tuner.error.GpBuildException("could not find suitable parameters.  This is probably due to a singlar matrix")
    } else {
      /*
      println("minimum found" 
           + " with log likelihood " + minLL
           + " at point " + minPt.map(math.exp(_)))
      */
      
      // One more time to get the final versions of the values
      //println("final value computation")
      val thetas = minPt map {math.exp(_)}
      //println("m " + corrMatrix(samples, thetas, alphas)(0,::))
      val (ll, mu, sig2, rInv) = logLikelihood(samples, responses, 
                                               thetas, alphas)
      (ll, mu, sig2, rInv, thetas, alphas)
    }
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
    var rDet = breeze.linalg.det(r)
    // If the determinant is 0 then try adding a little bit to the diagonal
    if(rDet == 0.0) {
      //throw new tuner.error.SingularMatrixException(r)
      r += DenseMatrix.eye[Double](r.rows) :* 1e-3
      rDet = breeze.linalg.det(r)
    }
    if(rDet == 0.0) {
      // If its still broken then give up
      throw new tuner.error.SingularMatrixException(r)
    }
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
  }

  def corrMatrix(samples:DenseMatrix[Double], 
                 thetas:DenseVector[Double], 
                 alphas:DenseVector[Double]) = {
    val mtx = DenseMatrix.eye[Double](samples.rows)
    for(r1 <- 0 until samples.rows) {
      for(r2 <- 0 until samples.rows) {
        if(r1 != r2) { // don't need to compute corr for the same point
          val corr = corrFunction(samples(r1,::).inner, 
                                  samples(r2,::).inner, 
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

