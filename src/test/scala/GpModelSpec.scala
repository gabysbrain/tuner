package tuner.test

import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import tuner.Table
import tuner.gp.NumberzGp
import tuner.gp.Rgp

import tuner.test.generator._
import tuner.test.util.Path

class GpModelSpec extends FunSuite 
                  with GeneratorDrivenPropertyChecks {

  import TableGen._

  test("should get an exception when estimating unknown fields") {
    forAll(tableGen suchThat {_.numRows>2}, tableGen suchThat {_.numRows > 2},
           minSuccessful(10)) {(train, test) =>

      val d = train.numFields
      val (inputFields, tmpOutputField) = train.fieldNames splitAt (d-1)
      val outputField = tmpOutputField.head
      val ngp = new NumberzGp
      val gp = ngp.buildModel(train,
                              inputFields, 
                              outputField, 
                              tuner.Config.errorField)
      try {
        gp.sampleTable(test)
        test.fieldNames.toSet == 
          (tuner.Config.errorField::outputField::inputFields).toSet
      } catch {
        case nmp:tuner.error.NonMatchingParameterException =>
          test.fieldNames.toSet != 
            (tuner.Config.errorField::outputField::inputFields).toSet
      }
    }
  }

  test("make sure both numberz and R code make similar predictions") {
    (pending)
    /*
    check(forAll(trainTestTableGen suchThat {_._1.numRows > 2}) {case (train,test) =>
     //val (train, test) = data
     val savePath = Path.random + ".csv"
     train.toCsv(savePath)
     val d = train.fieldNames.length
     val (inputFields, tmpOutputField) = train.fieldNames splitAt (d-1)
     val outputField = tmpOutputField.head
     val ngp = new NumberzGp
     val gp1 = ngp.buildModel(savePath,
                              inputFields, 
                              outputField, 
                              tuner.Config.errorField)
     val rgp = new Rgp
     val gp2 = rgp.buildModel(savePath,
                              inputFields, 
                              outputField, 
                              tuner.Config.errorField)
      
      // see what predictions we make
      val gp1Preds = gp1.sampleTable(test)
      val gp2Preds = gp2.sampleTable(test)
      var matches = true
      for(i <- 0 until gp1Preds.numRows) {
        val (r1, r2) = (gp1Preds.tuple(i), gp2Preds.tuple(i))
        matches = matches && r1(outputField) == r2(outputField)
      }
      matches
    })
    */
  }

}

