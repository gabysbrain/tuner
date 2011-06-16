package tuner.util

import java.io.File

object Path {
  def join(p1:String, p2:String) : String = {
    new File(p1, p2).getAbsolutePath
  }
}

