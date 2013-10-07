package test.tuner

object Util {

  def resource(file:String) : String = {
    val r = getClass.getResource(file)
    if(r == null)
      throw new java.io.IOException(s"resource '${file}' not found")
    r.getPath.toString
  }

}
