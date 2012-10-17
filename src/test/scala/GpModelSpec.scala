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

  implicit override val generatorDrivenConfig = 
    PropertyCheckConfig(minSuccessful=1000)

  test("should get an exception when estimating unknown fields") {
    forAll(tableGen suchThat {_.numRows>2}, tableGen suchThat {_.numRows > 2},
           minSuccessful(10)) {(train, test) =>

      val d = train.numFields
      val (inputFields, tmpOutputField) = train.fieldNames splitAt (d-1)
      val outputField = tmpOutputField.head
      val gp = NumberzGp.buildModel(train,
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

  test("make sure both numberz and R code have similar correlation params") {
    forAll(trainTestTableGen suchThat {_._1.numRows > 2}) {case (train,test) =>

     val savePath = Path.random + ".csv"
     train.toCsv(savePath)
     val d = train.fieldNames.length
     val (inputFields, tmpOutputField) = train.fieldNames splitAt (d-1)
     val outputField = tmpOutputField.head
     val gp1 = NumberzGp.buildModel(savePath,
                                    inputFields, 
                                    outputField, 
                                    tuner.Config.errorField)
     val rgp = new Rgp
     val gp2 = rgp.buildModel(savePath,
                              inputFields, 
                              outputField, 
                              tuner.Config.errorField)
      
      // see what predictions we make
      gp1.thetas == gp2.thetas
    }
  }

  test("make sure both numberz and R code make similar predictions") {
    forAll(trainTestTableGen suchThat {_._1.numRows > 2}) {case (train,test) =>

     val savePath = Path.random + ".csv"
     train.toCsv(savePath)
     val d = train.fieldNames.length
     val (inputFields, tmpOutputField) = train.fieldNames splitAt (d-1)
     val outputField = tmpOutputField.head
     val gp1 = NumberzGp.buildModel(savePath,
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
      gp1Preds == gp2Preds
    }
  }
}

