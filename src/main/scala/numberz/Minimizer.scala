package numberz

import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.optimization.GoalType
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer
import org.apache.commons.math3.optimization.direct.NelderMeadSimplex
import org.apache.commons.math3.optimization.direct.MultiDirectionalSimplex
import org.apache.commons.math3.optimization.direct.SimplexOptimizer

object Minimizer {

  def nelderMead(dims:Int, f:Array[Double]=>Double,
                 startPoint:Array[Double]=null) 
               : (Array[Double],Boolean) = {
    val func = new MultivariateFunction {
      def value(p:Array[Double]) : Double = f(p)
    }
    val start = if(startPoint==null) Array.fill(dims)(1.0) else startPoint
    val simplex = new SimplexOptimizer() with RetrievableOptimizer {
      override def computeObjectiveValue(p:Array[Double]) : Double =
        computeObjectiveValue(p, super.computeObjectiveValue)
    }
    val nm = new NelderMeadSimplex(dims)
    nm.build(start)
    simplex.setSimplex(nm)

    try {
      (simplex.optimize(100, func, GoalType.MINIMIZE, start).getPoint, true)
    } catch {
      case tme:org.apache.commons.math3.exception.TooManyEvaluationsException =>
        (simplex.lastMinimum, false)
    }
  }

  def multiDirectional(dims:Int, f:Array[Double]=>Double, 
                       startPoint:Array[Double]=null)
                     : (Array[Double],Boolean) = {
    val func = new MultivariateFunction {
      def value(p:Array[Double]) : Double = f(p)
    }
    val start = if(startPoint==null) Array.fill(dims)(1.0) else startPoint
    val simplex = new SimplexOptimizer() with RetrievableOptimizer {
      override def computeObjectiveValue(p:Array[Double]) : Double =
        computeObjectiveValue(p, super.computeObjectiveValue)
    }
    val md = new MultiDirectionalSimplex(dims)
    md.build(start)
    simplex.setSimplex(md)

    try{
      (simplex.optimize(100, func, GoalType.MINIMIZE, start).getPoint, true)
    } catch {
      case tme:org.apache.commons.math3.exception.TooManyEvaluationsException =>
        (simplex.lastMinimum, false)
    }
  }

  def bobyqa(dims:Int, 
             f:Array[Double]=>Double, 
             bounds:Array[(Double,Double)],
             startPoint:Array[Double]=null)
            : (Array[Double],Boolean) = {
    val func = new MultivariateFunction {
      def value(p:Array[Double]) : Double = f(p)
    }
    val start = if(startPoint == null) Array.fill(dims)(1.0) else startPoint
    val byob = new BOBYQAOptimizer(dims+2) with RetrievableOptimizer {
      override def computeObjectiveValue(p:Array[Double]) : Double =
        computeObjectiveValue(p, super.computeObjectiveValue)
    }
    val (lower,upper) = bounds.unzip

    try {
      (byob.optimize(100, func, GoalType.MINIMIZE, 
                     start, lower.toArray, upper.toArray).getPoint, true)
    } catch {
      case tse:org.apache.commons.math3.exception.NumberIsTooSmallException =>
        if(dims < 2) 
          throw new Exception("must have at least 2 dimensions", tse)
        else
          throw tse
      case tme:org.apache.commons.math3.exception.TooManyEvaluationsException =>
        (byob.lastMinimum, false)
    }
  }
}

