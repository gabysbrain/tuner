package tuner.test.generator

import org.scalacheck._

import tuner.GpModel
import tuner.Rgp

object GpModelGen {

  def gpModelType : Gen[GpModel] = for {
    data <- TableGen.tableType
    fields = data.fieldNames
    inD <- Gen.choose(1, fields.length-1)
    inFields = fields.take(inD)
    outFieldId <- Gen.choose(inD, fields.length-1)
    outField = fields(outFieldId)
    dataPath = tuner.test.util.Path.random + ".csv"
  } yield {
    data.toCsv(dataPath)
    val rgp = new Rgp(dataPath)
    rgp.buildModel(inFields, outField, tuner.Config.errorField)
  }

}

