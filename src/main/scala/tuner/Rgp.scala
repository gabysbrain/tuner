package tuner

import org.rosuda.JRI.RList
import org.rosuda.JRI.REXP

import scala.io.Source

object Rgp {
  val DESIGNRVAR = "data.design"
  val ESTIMATERVAR = "data.estimates"
  val MODELRVAR = "gp.fit"

  /*
  def main(args:Array[String]) = {
    val gp = new Rgp("data/temp_design.txt")
    val model = gp.buildModel(List("alpha","sigma"), "dice", "dace_stddev")
  }
  */
}

class Rgp(designFile:String) {
  
  val (imageDir, sampleFile) = splitFile(designFile)

  // Setup the sparkle gp stuff
  R.runCommand("source('%s')".format(Config.gpRScript))

  def buildModel(paramFields:List[String], 
                 responseField:String, 
                 errorField:String) : GpModel = {
    print("building model...")
    // Read the design file

    val rDesignFile = designFile.replace("\\", "/")
    R.runCommand("%s <- read.design('%s')".format(Rgp.DESIGNRVAR, rDesignFile))
    // Create an r vector of all the strings
    val rvect = "c(" + paramFields.map("'"+_+"'").reduceLeft(_+","+_) + ")"
    //println("params: " + rvect)
    val fit = R.runCommand("%s <- fit.model(%s, %s, '%s')".
      format(Rgp.MODELRVAR, Rgp.DESIGNRVAR, rvect, responseField))
    println("done")

    new GpModel(fit.asList, paramFields, responseField, errorField)
  }

  // Splits a file into its directory and filename components
  private def splitFile(fname:String) : (String,String) = {
    import java.io.File
    val f = new File(fname)
    (f.getParent, f.getName)
  }
}

