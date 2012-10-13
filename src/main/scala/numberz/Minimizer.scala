package numberz

import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.optimization.GoalType
import org.apache.commons.math3.optimization.direct.BOBYQAOptimizer
import org.apache.commons.math3.optimization.direct.NelderMeadSimplex
import org.apache.commons.math3.optimization.direct.MultiDirectionalSimplex
import org.apache.commons.math3.optimization.direct.SimplexOptimizer

object Minimizer {

  def nelderMead(dims:Int, f:Array[Double]=>Double) : Array[Double] = {
    val func = new MultivariateFunction {
      def value(p:Array[Double]) : Double = f(p)
    }
    val start = Array.fill(dims)(1.0)
    val simplex = new SimplexOptimizer()
    val nm = new NelderMeadSimplex(dims)
    nm.build(start)
    simplex.setSimplex(nm)

    simplex.optimize(100, func, GoalType.MINIMIZE, start).getPoint
  }

  def multiDirectional(dims:Int, f:Array[Double]=>Double) : Array[Double] = {
    val func = new MultivariateFunction {
      def value(p:Array[Double]) : Double = f(p)
    }
    val start = Array.fill(dims)(1.0)
    val simplex = new SimplexOptimizer()
    val md = new MultiDirectionalSimplex(dims)
    md.build(start)
    simplex.setSimplex(md)

    simplex.optimize(100, func, GoalType.MINIMIZE, start).getPoint
  }

  def bobyqa(dims:Int, 
             f:Array[Double]=>Double, 
             bounds:Array[(Double,Double)]) : Array[Double] = {
    val func = new MultivariateFunction {
      def value(p:Array[Double]) : Double = f(p)
    }
    val start = Array.fill(dims)(1.0)
    val byob = new BOBYQAOptimizer(dims+2)
    val (lower,upper) = bounds.unzip

    try {
      // TODO: find a better way than just increasing the number of iterations
      byob.optimize(1000, func, GoalType.MINIMIZE, 
                    start, lower.toArray, upper.toArray).getPoint
    } catch {
      case tse:org.apache.commons.math3.exception.NumberIsTooSmallException =>
        if(dims < 2) 
          throw new Exception("must have at least 2 dimensions", tse)
        else
          throw tse
    }
  }
}

