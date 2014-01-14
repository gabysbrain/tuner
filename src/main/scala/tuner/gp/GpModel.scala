package tuner.gp

import breeze.linalg._
import breeze.numerics._

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import tuner.Config
import tuner.DimRanges
import tuner.Grid2D
import tuner.Sampler
import tuner.Table
import tuner.error.ProjectLoadException
import tuner.util.Util

case class GpSpecification(
  responseDim:String,
  dimNames:List[String],
  thetas:List[Double],
  alphas:List[Double],
  mean:Double,
  sigma2:Double,
  designMatrix:List[List[Double]],
  responses:List[Double],
  invCorMtx:List[List[Double]]
)

object GpModel {
  def fromJson(json:GpSpecification) = {
    if(json.sigma2.isNaN) {
      throw new ProjectLoadException("sigma2 parameter cannot be NaN", null)
    }
    if(json.sigma2 == 0) {
      throw new ProjectLoadException("sigma2 parameter cannot be 0", null)
    }
    val resps = DenseVector(json.responses.toArray)
    val design = DenseMatrix(json.designMatrix.map(_.toArray):_*)
    val invCov = DenseMatrix(json.invCorMtx.map(_.toArray):_*)
    //println(json.dimNames)
    //println(design)
    //println(resps)
    // Make sure the arrays are the proper size
    if(design.rows != resps.length) {
      throw new ProjectLoadException(s"design matrix has ${design.rows} rows but there are ${resps.length} responses", null)
    }
    if(design.cols != json.thetas.length) {
      throw new ProjectLoadException(s"design matrix has ${design.cols} columns but there are ${json.thetas.length} dimensions", null)
    }
    if(invCov.rows != resps.length) {
      throw new ProjectLoadException(s"covariance matrix has ${invCov.rows} rows but there are ${resps.length} responses", null)
    }
    if(invCov.cols != resps.length) {
      throw new ProjectLoadException(s"covariance matrix has ${invCov.cols} columns but there are ${resps.length} responses", null)
    }
       
    new GpModel(DenseVector(json.thetas.toArray), 
                DenseVector(json.alphas.toArray), 
                json.mean, json.sigma2,
                design, resps, invCov,
                json.dimNames, json.responseDim, Config.errorField)
  }
}

// A gp model takes a sampling density and returns 
// a filename from which to read the sampled data
//type Model = Int => String
class GpModel(val thetas:DenseVector[Double], 
              val alphas:DenseVector[Double], 
              val mean:Double, val sig2:Double, 
              val design:DenseMatrix[Double], 
              val responses:DenseVector[Double], 
              val rInverse:DenseMatrix[Double], 
              val dims:List[String], val respDim:String, val errDim:String) {

  // Automatically compute sig2
  def this(thetas:DenseVector[Double], alphas:DenseVector[Double], mean:Double, 
           design:DenseMatrix[Double], responses:DenseVector[Double], 
           rInverse:DenseMatrix[Double], 
           dims:List[String], respDim:String, errDim:String) =
    this(thetas, alphas, mean, 
         {
           val diff = responses - mean
           (diff dot (rInverse * diff)) / responses.length
         },
         design, responses, rInverse, dims, respDim, errDim)

  // Automatically compute mu and sig2
  def this(thetas:DenseVector[Double], alphas:DenseVector[Double], 
           design:DenseMatrix[Double], responses:DenseVector[Double], 
           rInverse:DenseMatrix[Double], 
           dims:List[String], respDim:String, errDim:String) =
    this(thetas, alphas, 
         (DenseVector.ones[Double](responses.length) dot (rInverse * responses)) /
           (DenseVector.ones[Double](responses.length) dot (rInverse * DenseVector.ones[Double](responses.length))),
         design, responses, rInverse, dims, respDim, errDim)

  // Also precompute rInverse . (responses - mean)
  val corrResponses = rInverse * (responses - mean)
  //println("cr2: " + corrResponses)
  //println("rs: " + responses)
  //println("ri: " + rInverse)

  def toJson = {
    GpSpecification(
      respDim, 
      dims.toList, 
      thetas.toArray.toList, 
      alphas.toArray.toList, 
      mean, sig2,
      (0 until design.rows).map {r => 
        (0 until design.cols).map {c => design(r, c)} toList
      } toList,
      responses.toArray.toList, 
      (0 until rInverse.rows).map {r => 
        (0 until rInverse.cols).map {c => rInverse(r, c)} toList
      } toList)
  }

  def maxGain(range:DimRanges):Float = {
    var mx = Double.MinValue
    Sampler.lhc(range, Config.numericSampleDensity, pt => {
      val (est, err) = runSample(pt)
      val expgain = calcExpectedGain(est, err)
      mx = math.max(mx, expgain)
    })
    mx.toFloat
  }

  // Store the most recently seen max value for the function
  def funcMax:Float = responses.max.toFloat
  def funcMin:Float = responses.min.toFloat

  def theta(dim:String) = thetas(dims.indexOf(dim))

  def sampleSlice(rowDim:(String,(Float,Float)), 
                  colDim:(String,(Float,Float)),
                  slices:List[(String,Float)], 
                  numSamples:Int)
      : ((String, Grid2D), (String, Grid2D), (String, Grid2D)) = {
    
    val arrSlice = Array.fill(dims.length)(0.0)
    val sliceMap = slices.toMap
    dims.zipWithIndex.foreach {case (fld, i) =>
      arrSlice(i) = sliceMap(fld)
    }
    val xDim = dims.indexOf(rowDim._1)
    val yDim = dims.indexOf(colDim._1)

    // generate one matrix from scratch and then copy the rest
    val startTime = System.currentTimeMillis
    val response = Sampler.regularSlice(rowDim, colDim, numSamples)
    val errors = new Grid2D(response.rowIds, response.colIds)
    val gains = new Grid2D(response.rowIds, response.colIds)

    response.rowIds.zipWithIndex.foreach {tmpx =>
      val (xval,x) = tmpx
      response.colIds.zipWithIndex.foreach {tmpy =>
        val (yval, y) = tmpy
        arrSlice(xDim) = xval
        arrSlice(yDim) = yval
        val (est, err) = estimatePoint(DenseVector(arrSlice))
        response.set(x, y, est.toFloat)
        errors.set(x, y, err.toFloat)
        val expgain = calcExpectedGain(est, err)
        if(!expgain.isNaN) {
          gains.set(x, y, expgain.toFloat)
        } else {
          gains.set(x, y, 0f)
        }
      }
    }

    val endTime = System.currentTimeMillis
    ((respDim, response), 
     (Config.errorField, errors), 
     (Config.gainField, gains))
  }

  def sampleTable(samples:Table) : Table = {
    // verify that the input table has the proper fields
    val sampleFields:Set[String] = samples.fieldNames.toSet
    if(!(dims.toSet subsetOf sampleFields)) {
      val missingFields:Set[String] = dims.toSet -- sampleFields
      throw new tuner.error.NonMatchingParameterException(missingFields toList)
    }

    val outTbl = new Table
    for(r <- 0 until samples.numRows) {
      val tpl = samples.tuple(r)
      val (est, err) = runSample(tpl)
      outTbl.addRow((respDim, est.toFloat)::(errDim, err.toFloat)::tpl.toList)
    }
    outTbl
  }

  def runSample(pt:List[(String, Float)]) : (Double, Double) = 
    runSample(pt.toMap)

  def runSample(pt:Table.Tuple) : (Double, Double) = {
    val xx = dims.map(pt.get(_)).flatten.map(_.toDouble)
    val (est, err) = estimatePoint(DenseVector(xx.toArray))
    //curFuncMax = math.max(est.toFloat, curFuncMax)
    (est, err)
  }

  // Compute the correlation wrt each design point
  private def estimatePoint(point:DenseVector[Double]) : (Double,Double) = {
    val ptCors = DenseVector.zeros[Double](design.rows)
    for(r <- 0 until design.rows) {
      ptCors.update(r, corrFunction(design(r, ::).toDenseVector, point))
    }
    //println("pc: " + ptCors.toString)
    //println("cr: " + corrResponses.toString)
    val est = mean + sig2 * (ptCors dot corrResponses)
    val err = sig2 * (1 - sig2 * (ptCors dot (rInverse * ptCors)))
    if(err < 0) (est, 0)
    else        (est, math.sqrt(err))
  }

  private def corrFunction(p1:DenseVector[Double], 
                           p2:DenseVector[Double]) : Double = {
    var sum:Double = 0
    for(d <- 0 until p1.length) {
      sum += corrFunction(p1(d), p2(d), thetas(d), alphas(d))
    }
    math.exp(-sum)
  }

  private def corrFunction(x1:Double, x2:Double, 
                           theta:Double, alpha:Double) : Double = {
    theta * math.pow(math.abs(x1 - x2), alpha)
  }

  def gradient(pt:List[(String,Float)]) : List[(String,Float)] = {
    val Epsilon:Float = 1e-9.toFloat
    val outVals = Array.fill(pt.length)(0f)
    val (val1, _) = runSample(pt)
    for(d <- 0 until pt.length) {
      val p2 = pt.take(d) ++ List((pt(d)._1, pt(d)._2+Epsilon)) ++ pt.drop(d)
      val p3 = pt.take(d) ++ List((pt(d)._1, pt(d)._2-Epsilon)) ++ pt.drop(d)
      val (val2, _) = runSample(p2)
      val (val3, _) = runSample(p3)
      val g2 = (val1 - val2) / Epsilon
      val g3 = (val1 - val3) / Epsilon
      outVals(d) = if(math.abs(g2) > math.abs(g3)) g2.toFloat else g3.toFloat
    }
    pt.zip(outVals).map {case ((fld,_),v2) => (fld, v2)}
  }

  def calcExpectedGain(est:Double, stddev:Double) : Double = {
    if(est.isNaN) {
      throw new Exception("estimate is NaN")
    }
    // These will come in handy later
    def erf(v:Double) : Double = {
      val a = (8*(math.Pi - 3)) / (3*math.Pi*(4 - math.Pi))
      val tmp = (4 / math.Pi + a * v * v) / (1 + a * v * v)
      v/math.abs(v) * math.sqrt(1 - math.exp(-(v*v) * tmp))
    }
    def pdf(v:Double) : Double = 1/(2*math.Pi) * math.exp(-(v*v) / 2)
    def cdf(v:Double) : Double = 0.5 * (1 + erf(v / math.sqrt(2)))

    if(stddev == 0) {
      //Double.MaxValue
      10000.0
    } else {
      val curFuncMax = funcMax
      val t1 = (est - curFuncMax)
      val t2 = cdf((est - curFuncMax) / stddev)
      val t3 = stddev * pdf((est - curFuncMax) / stddev)
      //println(s"${t1} ${t2} ${t3} ${est} ${stddev}")

      math.abs(t1 * t2 + t3)
    }
  }

  def crossValidate : (Vector[Double], Vector[Double]) = {
    val predResps = DenseVector.zeros[Double](design.rows)
    val predErrs = DenseVector.zeros[Double](design.rows)

    val origR = breeze.linalg.inv(rInverse)
    for(row <- 0 until design.rows) {
      val testSample = design(row, ::).toDenseVector
      val testResp = responses(row)

      // Extract all the new model building bits
      val newModelRows = (0 until design.rows).filterNot(_ == row)
      val newDesign = design(newModelRows, ::).toDenseMatrix
      val newRInv = breeze.linalg.inv(origR(newModelRows, newModelRows))
      val newResps = responses(newModelRows).toDenseVector
      val newGp = new GpModel(thetas, alphas, 
                              mean, sig2,
                              newDesign, newResps, 
                              newRInv, 
                              dims, respDim, errDim)

      val (predResp, predErr) = newGp.estimatePoint(testSample)
      predResps.update(row, predResp)
      predErrs.update(row, predErr)
    }

    (predResps, predErrs)
  }

  def validateModel : (Boolean, Vector[Double]) = {
    val (preds, vars) = crossValidate
    val sds = (responses - preds) / vars
    (sds.map {x => x > -3.0 && x < 3.0} all, sds)
  }

  override def equals(o:Any) : Boolean = o match {
    case that:GpModel => this.thetas == that.thetas &&
                         this.alphas == that.alphas &&
                         this.mean == that.mean &&
                         this.sig2 == that.sig2 &&
                         this.design == that.design &&
                         this.responses == that.responses
                         this.rInverse == that.rInverse &&
                         this.dims == that.dims &&
                         this.respDim == that.respDim &&
                         this.errDim == that.errDim
    case _ => false
  }

}
