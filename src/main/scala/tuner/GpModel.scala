package tuner

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._

import org.jblas.DoubleMatrix

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
    new GpModel(new DoubleMatrix(json.thetas.toArray), 
                new DoubleMatrix(json.alphas.toArray), 
                json.mean, json.sigma2,
                new DoubleMatrix(json.designMatrix.map(_.toArray).toArray),
                new DoubleMatrix(json.responses.toArray),
                new DoubleMatrix(json.invCorMtx.map(_.toArray).toArray),
                json.dimNames, json.responseDim, Config.errorField)
  }
}

// A gp model takes a sampling density and returns 
// a filename from which to read the sampled data
//type Model = Int => String
class GpModel(val thetas:DoubleMatrix, val alphas:DoubleMatrix, 
              val mean:Double, val sig2:Double, 
              val design:DoubleMatrix, val responses:DoubleMatrix, 
              val rInverse:DoubleMatrix, 
              val dims:Seq[String], val respDim:String, val errDim:String) {

  // Automatically compute sig2
  def this(thetas:DoubleMatrix, alphas:DoubleMatrix, mean:Double, 
           design:DoubleMatrix, responses:DoubleMatrix, 
           rInverse:DoubleMatrix, 
           dims:Seq[String], respDim:String, errDim:String) =
    this(thetas, alphas, mean,
         {
           val diff = responses.sub(mean)
           diff.dot(rInverse.mmul(diff)) / responses.length
         },
         design, responses, rInverse, dims, respDim, errDim)

  // Automatically compute mu and sig2
  def this(thetas:DoubleMatrix, alphas:DoubleMatrix, 
           design:DoubleMatrix, 
           responses:DoubleMatrix, rInverse:DoubleMatrix, 
           dims:Seq[String], respDim:String, errDim:String) =
    this(thetas, alphas, 
         DoubleMatrix.ones(responses.length).dot(rInverse.mmul(responses)) /
           DoubleMatrix.ones(responses.length).dot(rInverse.mmul(DoubleMatrix.ones(responses.length))),
         design, responses, rInverse, dims, respDim, errDim)

  def this(fm:RList, dims:Seq[String], respDim:String, errDim:String) =
    this(new DoubleMatrix(fm.at("beta").asDoubleArray),
         new DoubleMatrix(fm.at("a").asDoubleArray),
         fm.at("mu").asDouble,
         fm.at("sig2").asDouble,
         new DoubleMatrix(fm.at("X").asDoubleMatrix),
         new DoubleMatrix(fm.at("Z").asDoubleArray),
         new DoubleMatrix(fm.at("invVarMatrix").asDoubleMatrix),
         dims, respDim, errDim)

  // Also precompute rInverse . (responses - mean)
  val corrResponses = rInverse.mmul(responses.sub(mean))

  def toJson = {
    GpSpecification(
      respDim, 
      dims.toList, 
      thetas.toArray.toList, 
      alphas.toArray.toList, 
      mean, sig2,
      design.toArray2.map(_.toList).toList, 
      responses.toArray.toList, 
      rInverse.toArray2.map(_.toList).toList)
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
  def funcMax:Float = math.max(responses.max.toFloat, mean.toFloat)
  def funcMin:Float = math.min(responses.min.toFloat, mean.toFloat)

  def theta(dim:String) = thetas.get(dims.indexOf(dim))

  def sampleTable(samples:Table) : Table = {
    val outTbl = new Table
    for(r <- 0 until samples.numRows) {
      val tpl = samples.tuple(r)
      val (est, err) = runSample(tpl.toList)
      outTbl.addRow((respDim, est.toFloat)::(errDim, err.toFloat)::tpl.toList)
    }
    outTbl
  }

  def runSample(pt:List[(String, Float)]) : (Double, Double) = {
    val mapx = pt.toMap
    val xx = new DoubleMatrix(dims.map({mapx.get(_)}).flatten.map({_.toDouble}).toArray)
    val (est, err) = estimatePoint(xx)
    //curFuncMax = math.max(est.toFloat, curFuncMax)
    (est, err)
  }

  // Compute the correlation wrt each design point
  private def estimatePoint(point:DoubleMatrix) : (Double,Double) = {
    val ptCors = DoubleMatrix.zeros(design.rows)
    (0 until ptCors.length).foreach {i =>
      ptCors.put(i, corrFunction(design.getRow(i), point))
    }
    val est = mean + sig2 * ptCors.dot(corrResponses)
    val err = sig2 * (1 - sig2 * ptCors.dot(rInverse.mmul(ptCors)))
    if(err < 0) (est, 0)
    else        (est, math.sqrt(err))
  }

  private def corrFunction(p1:DoubleMatrix, p2:DoubleMatrix) : Double = {
    var sum = 0.0

    for(d <- 0 until p1.length) {
      sum += corrFunction(p1.get(d), p2.get(d), thetas.get(d), alphas.get(d))
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

  /*
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
  */

  def calcExpectedGain(est:Double, stddev:Double) : Double = {
    // These will come in handy later
    def erf(v:Double) : Double = {
      val a = (8*(math.Pi - 3)) / (3*math.Pi*(4 - math.Pi))
      val tmp = (4 / math.Pi + a * v * v) / (1 + a * v * v)
      v/math.abs(v) * math.sqrt(1 - math.exp(-(v*v) * tmp))
    }
    def pdf(v:Double) : Double = 1/(2*math.Pi) * math.exp(-(v*v) / 2)
    def cdf(v:Double) : Double = 0.5 * (1 + erf(v / math.sqrt(2)))

    val curFuncMax = funcMax
    val t1 = (est - curFuncMax)
    val t2 = cdf((est - curFuncMax) / stddev)
    val t3 = stddev * pdf((est - curFuncMax) / stddev)

    math.abs(t1 * t2 + t3)
  }

  // NOTE: This assumes the gaussian correlation model!
  /*
  def levelSets(c:Float) : List[DimRanges] = {
    // Start with just generating everything
    // We'll filter later
    val glen = corrResponses.dot(corrResponses)
    val ginv = corrResponses.map {g => g / glen}
    val initialSets = design.zip(ginv).map({tmp =>
      val (designPt, g) = tmp
      val dimRanges = dims.zipWithIndex.map {tmp2 =>
        val (field, i) = tmp2
        val theta = thetas(i)
        val centerpt = designPt(i)
        val r2 = math.log((c - mean) * g) / (-theta)
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
  */

}
