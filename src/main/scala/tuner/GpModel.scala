package tuner

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import org.apache.commons.math.analysis.DifferentiableMultivariateRealFunction
import org.apache.commons.math.analysis.MultivariateRealFunction
import org.apache.commons.math.analysis.MultivariateVectorialFunction
import org.apache.commons.math.optimization.GoalType
import org.apache.commons.math.optimization.direct.MultiDirectional
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer
import org.apache.commons.math.optimization.general.NonLinearConjugateGradientOptimizer
import org.apache.commons.math.optimization.general.ConjugateGradientFormula


import org.rosuda.JRI.RList

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
    new GpModel(json.thetas, json.alphas, json.mean, json.sigma2,
                json.designMatrix.map(_.toArray).toArray,
                json.responses.toArray,
                json.invCorMtx.map(_.toArray).toArray,
                json.dimNames, json.responseDim, Config.errorField)
  }
}

// A gp model takes a sampling density and returns 
// a filename from which to read the sampled data
//type Model = Int => String
class GpModel(val thetas:List[Double], val alphas:List[Double], 
              val mean:Double, val sig2:Double, 
              val design:Array[Array[Double]], val responses:Array[Double], 
              val rInverse:Array[Array[Double]], 
              val dims:List[String], val respDim:String, val errDim:String) {

  def this(thetas:List[Double], alphas:List[Double], mean:Double, 
           design:Array[Array[Double]], responses:Array[Double], 
           rInverse:Array[Array[Double]], 
           dims:List[String], respDim:String, errDim:String) =
    this(thetas, alphas, mean, 
         LinAlg.dotProd(LinAlg.dotProd(rInverse, 
                                       responses.map({_ - mean})), 
                                       responses.map({_ - mean})) / responses.size,
         design, responses, rInverse, dims, respDim, errDim)

  def this(thetas:List[Double], alphas:List[Double], 
           design:Array[Array[Double]], 
           responses:Array[Double], rInverse:Array[Array[Double]], 
           dims:List[String], respDim:String, errDim:String) =
    this(thetas, alphas, 
         LinAlg.dotProd(LinAlg.dotProd(rInverse, responses), 
                                       LinAlg.ones(responses.size)) / 
          LinAlg.dotProd(LinAlg.dotProd(rInverse, LinAlg.ones(responses.size)), 
                         LinAlg.ones(responses.size)), 
         design, responses, rInverse, dims, respDim, errDim)

  def this(fm:RList, dims:List[String], respDim:String, errDim:String) =
    this(fm.at("beta").asDoubleArray.toList,
         fm.at("a").asDoubleArray.toList,
         fm.at("mu").asDouble,
         fm.at("sig2").asDouble,
         fm.at("X").asDoubleMatrix,
         fm.at("Z").asDoubleArray,
         fm.at("invVarMatrix").asDoubleMatrix,
         dims, respDim, errDim)

  // Also precompute rInverse . (responses - mean)
  val corrResponses = LinAlg.dotProd(rInverse, responses.map({_ - mean}))

  def maxGain(range:DimRanges):Float = {
    var mx:Float = Float.MinValue
    Sampler.lhc(range, Config.respHistogramSampleDensity, pt => {
      val (est, err) = runSample(pt)
      val expgain = calcExpectedGain(est.toFloat, err.toFloat)
      mx = math.max(mx, expgain)
    })
    mx
  }

  // Store the most recently seen max value for the function
  def funcMax:Float = responses.max.toFloat
  def funcMin:Float = responses.min.toFloat

  def sampleSlice(rowDim:(String,(Float,Float)), 
                  colDim:(String,(Float,Float)),
                  slices:List[(String,Float)], 
                  numSamples:Int)
      : ((String, Matrix2D), (String, Matrix2D), (String, Matrix2D)) = {
    
    val filteredSlice = slices.filter {x =>
      x._1 != rowDim._1 && x._1 != colDim._1
    }
    // generate one matrix from scratch and then copy the rest
    val startTime = System.currentTimeMillis
    val response = Sampler.regularSlice(rowDim, colDim, numSamples)
    val errors = new Matrix2D(response.rowIds, response.colIds)

    response.rowIds.zipWithIndex.foreach {tmpx =>
      val (xval,x) = tmpx
      response.colIds.zipWithIndex.foreach {tmpy =>
        val (yval, y) = tmpy
        val pt = (rowDim._1, xval) :: (colDim._1, yval) :: filteredSlice
        val (est, err) = runSample(pt)
        response.set(x, y, est.toFloat)
        errors.set(x, y, err.toFloat)
      }
    }

    val endTime = System.currentTimeMillis
    //println("pred time: " + (endTime - startTime) + "ms")
    ((respDim, response), 
     (Config.errorField, errors), 
     (Config.gainField, calcExpectedGain(response, errors)))
  }

  def sampleTable(samples:Table) : Table = {
    val outTbl = new Table
    for(r <- 0 until samples.numRows) {
      val tpl = samples.tuple(r)
      val (est, err) = runSample(tpl.toList)
      outTbl.addRow((respDim, est.toFloat)::(errDim, err.toFloat)::tpl.toList)
    }
    outTbl
  }

  /*
  def sampleAnova(dimRanges:DimRanges,
                  rowDim:(String,(Float,Float)), 
                  colDim:(String,(Float,Float)),
                  numSamples:Int = Config.estimateSampleDensity)
        : Matrix2D = {
    val response = Sampler.regularSlice(rowDim, colDim, numSamples)
    val anovaFun = anova(dimRanges, List(rowDim._1, colDim._1))

    response.rowIds.zipWithIndex.foreach {tmpx =>
      val (xval,x) = tmpx
      response.colIds.zipWithIndex.foreach {tmpy =>
        val (yval, y) = tmpy
        val pt = List((rowDim._1, xval.toDouble), (colDim._1, yval.toDouble))
        val est = anovaFun(pt)
        response.set(x, y, est.toFloat)
      }
    }
    response
  }
  */

  def runSample(pt:List[(String, Float)]) : (Double, Double) = {
    val mapx = pt.toMap
    val xx = dims.map({mapx.get(_)}).flatten.map({_.toDouble}).toArray
    val (est, err) = estimatePoint(xx)
    //curFuncMax = math.max(est.toFloat, curFuncMax)
    (est, err)
  }

  // Compute the correlation wrt each design point
  private def estimatePoint(point:Array[Double]) : (Double,Double) = {
    var ptCors = design.map({corrFunction(_,point)})
    var est = mean + sig2 * LinAlg.dotProd(ptCors, corrResponses)
    var err = sig2 * (1 - sig2 * LinAlg.dotProd(ptCors, 
                                   LinAlg.dotProd(rInverse, ptCors)))
    if(err < 0) (est, 0)
    else        (est, math.sqrt(err))
  }

  private def corrFunction(p1:Array[Double], p2:Array[Double]) : Double = {
    //println("corr: d1: " + p1.size + " d2: " + p2.size)
    var sum:Double = 0
    for(d <- 0 until p1.size) {
      sum += corrFunction(p1(d), p2(d), thetas(d), alphas(d))
      //println("t: " + thetas(d) + " a: " + alphas(d))
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

  def calcExpectedGain(ests:Matrix2D, errs:Matrix2D) : Matrix2D = {
    val funcMax = ests.max
    val gainMatrix = new Matrix2D(ests.rowIds, ests.colIds)
    for(row <- 0 until ests.rows) {
      for(col <- 0 until ests.columns) {
        val est = ests.get(row, col)
        val stddev = errs.get(row, col)

        //println("t1 " + t1 + " t2 " + t2 + " t3 " + t3)
        val expgain = calcExpectedGain(est, stddev)
        if(!expgain.isNaN) {
          gainMatrix.set(row, col, expgain.toFloat)
        } else {
          gainMatrix.set(row, col, 0f)
        }
      }
    }
    gainMatrix
  }

  // Compute the density for a set of dimensions
  // This is assuming the GP model
  def anova(ranges:DimRanges, fields:List[String]) 
        : List[(String,Double)] => Double = {
    val indexedDims = dims.zipWithIndex.toMap
    val remainingDims = ranges.dimNames.diff(fields)
    val calcIdx = fields.map {indexedDims(_)}
    val intIdx = remainingDims.map {indexedDims(_)}
    val intRanges = remainingDims.map {fld => ranges.range(fld)}

    // I need a slightly different correlation function here
    // combining the two gives wacky results...
    def myCorrFunction(x1:Array[Double], x2:Array[Double], 
                       t:List[Double], a:List[Double]) : Double = {
      var prod:Double = 1
      for(d <- 0 until x1.size) {
        prod *= math.exp(-corrFunction(x1(d), x2(d), t(d), a(d)))
      }
      prod
    }

    // integrated out effects
    val rBar = design.map {designPt =>
      val intDims = intIdx.map {i => (designPt(i), thetas(i), alphas(i))}
      intDims.zip(intRanges).map({tmp =>
        val ((xd, t, a), (mn, mx)) = tmp
        //println(xd + " " + t + " " + a + " " + mn + " " + mx)
        LinAlg.simpsonsRule(
          {x => math.exp(-corrFunction(x, xd, t, a))}, mn, mx)
      }).product
    }

    def computeAnova(pt:List[(String,Double)]) : Double = {
      val mappedPt = pt.toMap
      val vals = fields.map({fld => mappedPt(fld)}).toArray
      // Dimensions we want integrated out
      val ptCors = design.zip(rBar).map {tmp =>
        val (designPt:Array[Double], restR:Double) = tmp
        val des = calcIdx.map({designPt(_)}).toArray
        val as = calcIdx.map({alphas(_)})
        val ts = calcIdx.map({thetas(_)})
        myCorrFunction(vals, des, as, ts) * restR
      }
      //sig2 * LinAlg.dotProd(ptCors, corrResponses)
      mean + sig2 * LinAlg.dotProd(ptCors, corrResponses)
    }
    computeAnova
  }

  def calcExpectedGain(est:Float, stddev:Float) : Float = {
    // These will come in handy later
    def erf(v:Double) : Double = {
      val a = (8*(math.Pi - 3)) / (3*math.Pi*(4 - math.Pi))
      val tmp = (4 / math.Pi + a * v * v) / (1 + a * v * v)
      v/math.abs(v) * math.sqrt(1 - math.exp(-(v*v) * tmp))
    }
    def pdf(v:Float) : Double = 1/(2*math.Pi) * math.exp(-(v*v) / 2)
    def cdf(v:Float) : Double = 0.5 * (1 + erf(v / math.sqrt(2)))

    val curFuncMax = funcMax
    val t1 = (est - curFuncMax).toFloat
    val t2 = cdf((est - curFuncMax) / stddev).toFloat
    val t3 = stddev * pdf((est - curFuncMax) / stddev).toFloat

    //println("t1 " + t1 + " t2 " + t2 + " t3 " + t3)
    math.abs(t1 * t2 + t3)
  }

  /*
  def computeMaxGain : (List[(String,Double)], Double) = {
    print("computing gain for " + respDim + "...")
    val energyFunc = new DifferentiableMultivariateRealFunction {
      def value(point:Array[Double]) : Double = {
        val (est, err) = estimatePoint(point)
        calcExpectedGain(est.toFloat, err.toFloat).toDouble
      }

      val gradient = new MultivariateVectorialFunction {
        def value(point:Array[Double]) : Array[Double] = {
          val pt = dims.zip(point).map {case (fld,v) => (fld,v.toFloat)}
          val (_, grad) = GpModel.this.gradient(pt).unzip
          grad.map(_.toDouble).toArray
        }
      }

      def partialDerivative(k:Int) = new MultivariateRealFunction {
        def value(point:Array[Double]) : Double = {
          val grad = gradient.value(point)
          grad(k)
        }
      }
    }
    val optim = new org.apache.commons.math.optimization.direct.PowellOptimizer
    val maxPointId = Util.maxIndex(responses)
    val optimPoint = optim.optimize(energyFunc, 
                                    GoalType.MAXIMIZE, 
                                    Array.fill(design(0).length)(0.5))
                                    //design(maxPointId))
    println("done")
    println("optim: " + optimPoint.getValue)
    (dims.zip(optimPoint.getPointRef), optimPoint.getValue)
  }
  */

  // NOTE: This assumes the gaussian correlation model!
  def levelSets(c:Float) : List[DimRanges] = {
    // Start with just generating everything
    // We'll filter later
    val glen = LinAlg.dotProd(corrResponses, corrResponses)
    val ginv = corrResponses.map {g => g / glen}
    val initialSets = design.zip(ginv).map({tmp =>
      val (designPt, g) = tmp
      val dimRanges = dims.zipWithIndex.map {tmp2 =>
        val (field, i) = tmp2
        val theta = thetas(i)
        val centerpt = designPt(i)
        val r2 = math.log((c - mean) * g) / (-theta)
        //println("r2: " + r2)
        //println("r: " + r)
        if(r2 >= 0) {
          val r = math.sqrt(r2)
          Some((field, ((centerpt - r).toFloat, (centerpt + r).toFloat)))
        } else {
          None
        }
      }
      if(dimRanges.exists {x => x == None}) {
        None
      } else {
        Some(new DimRanges(dimRanges.flatten.toMap))
      }
    }).toList.flatten
    // Now combine all the ranges we can
    dims.foldLeft(initialSets) {(curSet, field) =>
      val sortedSets = curSet.sortBy {x => x.range(field)}
      // Now join together any sets where the dimension ranges overlap
      sortedSets.foldLeft(Nil:List[DimRanges]) {(curList, dr) =>
        if(curList == Nil) {
          dr :: Nil
        } else {
          if(dr.min(field) <= curList.head.max(field)) {
            curList.head.merge(dr) :: curList.tail
          } else {
            dr :: curList
          }
        }
      }
    }
  }

}
