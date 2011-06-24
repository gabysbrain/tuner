package tuner.util

import scala.io.Source

object FileReader {

  def read(path:String) = {
    val lines = Source.fromFile(path).getLines
    //Source.fromFile(path).getLines.filter {ln => ln != ""} toIterator
    lines.filter {ln => ln != ""}
  }
  
}

