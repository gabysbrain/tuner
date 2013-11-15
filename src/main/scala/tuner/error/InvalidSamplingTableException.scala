package tuner.error

import tuner.Table

class InvalidSamplingTableException(val input:Table, val output:Table)
  extends Exception(
    if(!input.fieldNames.toSet.subsetOf(output.fieldNames.toSet)) {
      val inputFields = input.fieldNames.mkString(", ")
      val outputFields = output.fieldNames.mkString(", ")
      s"Input table fields ($inputFields) does not match output table fields ($outputFields)"
    } else {
      "Input table does not match output table"
    }
  )

