package tuner.test

import org.scalacheck._
//import org.scalacheck.Prop.forAll
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import tuner.Rgp
import tuner.Table

import tuner.test.generator._
import tuner.test.util._

class GpModelSpec extends FunSuite with Checkers {

  test("test creation of a gp model from data") {
    check(Prop.forAll(TableGen.tableType suchThat (_.numRows>2)) {data:Table =>
        val savePath = Path.random + ".csv"
        data.toCsv(savePath)
        val d = data.fieldNames.length
        val (inputFields, outputField) = data.fieldNames splitAt (d-1)
        val rgp = new Rgp(savePath)
        val gp = rgp.buildModel(inputFields, 
                                outputField.head, 
                                tuner.Config.errorField)
        inputFields.toSet == gp.dims.toSet
    })
  }

  test("test the evaluation of the gp model") {
    (pending)
  }


}

