package tuner.test

import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.Checkers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import tuner.Table
import tuner.gp.Rgp

import tuner.test.generator._
import tuner.test.util.Path

class RgpSpec extends FunSuite 
              with GeneratorDrivenPropertyChecks 
              with ShouldMatchers {

  import TableGen._

  test("test creation of a gp model from data using R") {
    forAll(TableGen.tableGen suchThat (_.numRows>3), minSuccessful(10)) 
    {data:Table =>
      val savePath = Path.random + ".csv"
      data.toCsv(savePath)
      val d = data.fieldNames.length
      val (inputFields, outputField) = data.fieldNames splitAt (d-1)
      val rgp = new Rgp
      val gp = rgp.buildModel(savePath,
                              inputFields, 
                              outputField.head, 
                              tuner.Config.errorField)

      inputFields.toSet should be === gp.dims.toSet
      inputFields.length should be === gp.design.columns
      data.numRows should be === gp.design.rows
    }
  }
}

