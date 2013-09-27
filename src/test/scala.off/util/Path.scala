package tuner.test.util

object Path {
  
  def random : String = {
    val f = java.io.File.createTempFile("tuner", "tmp")
    f.delete
    f.getAbsolutePath
  }
}

