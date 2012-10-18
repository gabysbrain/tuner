package tuner.test

import org.scalacheck._
import org.scalatest.FunSuite
import org.scalatest.prop.Checkers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import tuner.Table
import tuner.gp.NumberzGp

import tuner.test.generator._
import tuner.test.util.Path

class GpModelSpec extends FunSuite 
                  with GeneratorDrivenPropertyChecks {

  import TableGen._

  //implicit override val generatorDrivenConfig = 
    //PropertyCheckConfig(minSuccessful=1000)

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
}

