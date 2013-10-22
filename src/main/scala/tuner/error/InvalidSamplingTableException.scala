package tuner.error

import tuner.Table

class InvalidSamplingTableException(val input:Table, val output:Table)
  extends Exception

