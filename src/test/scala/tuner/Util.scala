package test.tuner

object Util {

  def resource(file:String, exec:Boolean=false) : String = {
    val r = getClass.getResource(file)
    if(r == null)
      throw new java.io.IOException(s"resource '${file}' not found")

    if(exec) {
      val f = new java.io.File(r.getPath)
      f.setExecutable(true)
    }
    r.getPath.toString
  }

}
