package tuner.error

class RInitException(val msg:String, cause:Throwable) 
        extends Exception(msg, cause) {

  def this(msg:String) = this(msg, null)
}

