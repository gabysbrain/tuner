package tuner.error


class SamplingErrorException(errorCode:Int) extends Exception("sampling script exited with non-zero exit code: " + errorCode)

