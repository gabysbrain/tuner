package tuner.test

import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers

import tuner.Table
import tuner.gp.NumberzGp
import tuner.gp.Rgp

import tuner.test.generator._
import tuner.test.util.Path

class GpModelSpec extends FunSuite with Checkers {

  /*
    })
  }
  */

  /*
  test("make sure both numberz and R code match") {
    check(Prop.forAll(TableGen.tableGen suchThat (_.numRows>3)) {data:Table =>
        val savePath = Path.random + ".csv"
        data.toCsv(savePath)
        val d = data.fieldNames.length
        val (inputFields, outputField) = data.fieldNames splitAt (d-1)
        val ngp = new NumberzGp(savePath)
        val gp1 = ngp.buildModel(inputFields, 
                                 outputField.head, 
                                 tuner.Config.errorField)
        val rgp = new Rgp(savePath)
        val gp2 = rgp.buildModel(inputFields, 
                                 outputField.head, 
                                 tuner.Config.errorField)
        // Just check the most important params for now
        gp1.thetas == gp2.thetas
    })
  }
  */

}

