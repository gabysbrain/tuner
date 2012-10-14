package tuner.gp.test

import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import tuner.Table
import tuner.gp.NumberzGp
import numberz.Matrix
import numberz.Vector

import numberz.test.VectorGen
import tuner.test.generator._

class NumberzGpSpec extends FunSuite with Checkers {

  val dataThetaGen :Gen[(Table,Vector)] = Gen.sized {size => for {
    t <- TableGen.tableType(size)
    v <- VectorGen.positiveVectorType(t.numFields)
  } yield {
    (t, v)
  }}

  test("make sure my table -> matrix logic creates a properly dimensioned matrix") {
    check(Prop.forAll(TableGen.tableGen suchThat (_.numRows>2)) {data:Table =>
      val flds = data.fieldNames
      val mtx = Matrix.fromRowMajor(
        data map {tpl => flds.map {f=>tpl(f).toDouble}}
      )
      
      (flds.length == mtx.columns) && (data.numRows == mtx.rows)
    })
  }

  /*
  test("test for a bug where we would access a non-existant sample when creating the correlation matrix") {
    check(Prop.forAll(TableGen.wideTableGen suchThat {_.numRows > 2}) {data:Table =>
      val flds = data.fieldNames
      val mtx = Matrix.fromRowMajor(
        data map {tpl => flds.map {f=>tpl(f).toDouble}}
      )

      // Doesn't matter what the thetas and alphas are
      val gp = new NumberzGp
      // make sure this doesn't throw an exception
      try {
        gp.corrMatrix(mtx, Vector.ones(flds.length), Vector.ones(flds.length))
        true
      } catch {
        case ore:org.apache.commons.math3.exception.OutOfRangeException => 
          ore.printStackTrace
          false
      }
    })
  }
  */

  test("the correlation matrix should be square") {
    check(Prop.forAll(dataThetaGen suchThat {_._1.numRows > 2}) 
    {case (data:Table,thetas:Vector) =>
      val flds = data.fieldNames
      val alphas = Vector.fill(flds.length)(2.0)
      val mtx = Matrix.fromRowMajor(
        data map {tpl => flds.map {f=>tpl(f).toDouble}}
      )

      val gp = new NumberzGp
      val corrs = gp.corrMatrix(mtx, thetas, alphas)
      (corrs.rows == data.numRows)    &&
      (corrs.columns == data.numRows)
    })
  }

  test("the correlation matrix should have all 1s in the diagonal") {
    check(Prop.forAll(dataThetaGen suchThat {_._1.numRows > 2})
    {case (data:Table,thetas:Vector) =>
      val flds = data.fieldNames
      val alphas = Vector.fill(flds.length)(2.0)
      val mtx = Matrix.fromRowMajor(
        data map {tpl => flds.map {f=>tpl(f).toDouble}}
      )

      val gp = new NumberzGp
      val corrs = gp.corrMatrix(mtx, thetas, alphas)
      (corrs.trace == data.numRows)
    })
  }

  test("the correlation matrix should be positive definite") {
    check(Prop.forAll(dataThetaGen suchThat {_._1.numRows > 2})
    {case (data:Table,thetas:Vector) =>
      val flds = data.fieldNames
      val alphas = Vector.fill(flds.length)(2.0)
      val mtx = Matrix.fromRowMajor(
        data map {tpl => flds.map {f=>tpl(f).toDouble}}
      )

      val gp = new NumberzGp
      val corrs = gp.corrMatrix(mtx, thetas, alphas)
      // make sure this doesn't throw an exception
      try {
        corrs.chol
        true
      } catch {
        case ore:org.apache.commons.math3.linear.NonPositiveDefiniteMatrixException => 
          false
      }
    })
  }

  test("the correlation matrix should be invertable") {
    check(Prop.forAll(dataThetaGen suchThat {_._1.numRows > 2})
    {case (data:Table,thetas:Vector) =>
      val flds = data.fieldNames
      val alphas = Vector.fill(flds.length)(2.0)
      val mtx = Matrix.fromRowMajor(
        data map {tpl => flds.map {f=>tpl(f).toDouble}}
      )

      val gp = new NumberzGp
      val corrs = gp.corrMatrix(mtx, thetas, alphas)
      // make sure this doesn't throw an exception
      try {
        corrs.inverse
        true
      } catch {
        case ore:org.apache.commons.math3.linear.SingularMatrixException => 
          ore.printStackTrace
          false
      }
    })
  }

  test("thetas passed to logLikelihood must be strictly positive") {
    val dataBadThetaGen :Gen[(Table,Vector)] = Gen.sized {size => for {
      t <- TableGen.tableType(size)
      v <- VectorGen.vectorType(t.numFields)
    } yield {
      (t, v)
    }}

    check(Prop.forAll(dataBadThetaGen suchThat {_._1.numRows > 2})
    {case (data:Table,thetas:Vector) =>
      val flds = data.fieldNames
      val alphas = Vector.fill(flds.length)(2.0)
      val mtx = Matrix.fromRowMajor(
        data map {tpl => flds.map {f=>tpl(f).toDouble}}
      )
      val resps = Vector.fill(mtx.rows)(1.0)
      try {
        val gp = new NumberzGp
        gp.logLikelihood(mtx, resps, thetas, alphas)
        thetas.forall(_ > 0.0)
      } catch {
        case iae:tuner.error.NonPositiveThetaException => thetas.exists(_ <= 0.0)
      }
    })
  }
}

