package numberz

import org.apache.commons.math3.analysis.MultivariateFunction
import org.apache.commons.math3.optimization.GoalType
import org.apache.commons.math3.optimization.direct.NelderMeadSimplex
import org.apache.commons.math3.optimization.direct.SimplexOptimizer

object Optim {

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
}

