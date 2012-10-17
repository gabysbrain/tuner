package tuner.test

import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import tuner.Table
import tuner.gp.Rgp

import tuner.test.generator._
import tuner.test.util.Path

class RgpSpec extends FunSuite with Checkers {

  import TableGen._

  test("test creation of a gp model from data using R") {
    (pending)
    /*
    check(Prop.forAll(TableGen.tableGen suchThat (_.numRows>3)) {data:Table =>
        val savePath = Path.random + ".csv"
        data.toCsv(savePath)
        val d = data.fieldNames.length
        val (inputFields, outputField) = data.fieldNames splitAt (d-1)
        val rgp = new Rgp
        val gp = rgp.buildModel(savePath,
                                inputFields, 
                                outputField.head, 
                                tuner.Config.errorField)
        inputFields.toSet == gp.dims.toSet
    })
  */
  }
}

