package tuner.error

class NonMatchingParameterException(missingFields:List[String])
  extends Exception("fields: " + missingFields.reduceLeft(_+", "+_) + " are required for prediction")

