package tuner.util

object ResourceLoader {
  def fileResource(name:String) : String = {
    var tmpFile = java.io.File.createTempFile("sparkle", "R")
    val in = getClass.getResourceAsStream(name)
    var out = new java.io.FileOutputStream(tmpFile)
    val buf:Array[Byte] = Array.fill(1024)(0)
    var len = in.read(buf)
    while(len > 0) {
      out.write(buf, 0, len)
      len = in.read(buf)
    }
    out.close
    tmpFile.getAbsolutePath
  }
}

